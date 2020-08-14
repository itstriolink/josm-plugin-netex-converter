package org.openstreetmap.josm.plugins.netexconverter.util;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.geom.Area;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import org.openstreetmap.josm.data.DataSource;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.OsmPrimitiveType;
import org.openstreetmap.josm.data.osm.Relation;
import org.openstreetmap.josm.data.osm.RelationMember;
import org.openstreetmap.josm.data.osm.Way;

/**
 * helper class/methods for merging filtered data into new DataSet and Export
 * Layer
 */
public class CustomDataSetMerger {

	private DataSet sourceDataSet;
	private DataSet targetDataSet;

	private final HashMap<OsmPrimitiveType, List<OsmPrimitive>> dataToBeExported;
	private final HashMap<Long, LinkedList<OsmPrimitive>> mergeMap;

	//private final FileWriter fileWriter;
	//private final FileWriter fileWriter2;
	//private final PrintWriter printWriter;
	//private final PrintWriter printWriter2;

	public CustomDataSetMerger(){
		dataToBeExported = new HashMap<>();
		Arrays.asList(OsmPrimitiveType.values()).stream()
		.forEach(type -> dataToBeExported.put(type, new ArrayList<>()));

		mergeMap = new HashMap<>();

		//fileWriter = new FileWriter(new File("JOSM_NeTEx_Export_Layer_SelectedData.txt"));
		//printWriter = new PrintWriter(fileWriter);
		//fileWriter2 = new FileWriter(new File("JOSM_NeTEx_Export_Layer_DataSet.txt"));
		//printWriter2 = new PrintWriter(fileWriter2);
	}

	private void filterDataRelevantForExport() {
		sourceDataSet.allPrimitives().stream()
		.filter(OSMTagFilter::primitiveIsRelevantForExport)
		.forEach(primitive -> selectPrimitiveAndItsReferences(primitive));

		dataToBeExported.values().stream()
		.forEach(list -> list.sort(Comparator.comparing(OsmPrimitive::getId)));

		//dataToBeExported.values().stream()
		//        .flatMap(list -> list.stream())
		//       .forEach(p -> printWriter.println(p.toString()));
		//printWriter.close();
	}

	public void mergeRelevantData(DataSet sourceDataSet, DataSet targetDataSet) {
		this.sourceDataSet = sourceDataSet;
		this.targetDataSet = targetDataSet;

		filterDataRelevantForExport();

		targetDataSet.beginUpdate();
		try {

			dataToBeExported.values().stream()
			.flatMap(list -> list.stream())
			.forEach(p -> mergePrimitiveIntoDataSet(p, targetDataSet));

			cleanUpReferencesAfterMerge();
			mergeMetaData();

		}
		finally {
			targetDataSet.endUpdate();
		}

		//targetDataSet.allPrimitives().stream()
		//        .forEach(p -> printWriter2.println(p.toString()));
		//printWriter2.close();
	}

	private void selectPrimitiveAndItsReferences(OsmPrimitive primitive) {
		OsmPrimitiveType typeOfPrimitive = primitive.getType();

		if (primitiveAlreadySelectedForExport(primitive) == false) {
			selectPrimitiveForExport(primitive);

			if (typeOfPrimitive.equals(OsmPrimitiveType.WAY)) {
				selectNodesOfWay((Way) primitive);
			}
			else if (typeOfPrimitive.equals(OsmPrimitiveType.RELATION)) {
				selectMembersOfRelation((Relation) primitive);
			}
		}

	}

	private void selectNodesOfWay(Way way) {
		way.getNodes().stream()
		.filter(node -> primitiveAlreadySelectedForExport(node) == false)
		.forEach(node -> selectPrimitiveForExport(node));
	}

	private void selectMembersOfRelation(Relation rel) {
		rel.getMembers().stream()
		.map(member -> member.getMember())
		.filter(member -> primitiveAlreadySelectedForExport(member) == false)
		.forEach(member -> selectPrimitiveAndItsReferences(member));
	}

	private void selectPrimitiveForExport(OsmPrimitive primitive) {
		dataToBeExported.get(primitive.getType()).add(primitive);
	}

	private boolean primitiveAlreadySelectedForExport(OsmPrimitive primitive) {
		return dataToBeExported.get(primitive.getType()).stream()
				.filter(p -> primitive.equals(p))
				.findFirst()
				.isPresent();
	}

	private void mergePrimitiveIntoDataSet(OsmPrimitive source, DataSet targetDataSet) {
		OsmPrimitiveType sourceType = source.getType();
		long sourceId = source.getId();
		OsmPrimitive target;
		LinkedList<OsmPrimitive> sourceTargetMap = new LinkedList<>();

		if (mergeMap.containsKey(sourceId)) {
			return;
		}

		switch (sourceType) {
		case NODE:
			target = source.isNew() ? new Node() : new Node(sourceId);
			break;
		case WAY:
			target = source.isNew() ? new Way() : new Way(sourceId);
			break;
		case RELATION:
			target = source.isNew() ? new Relation() : new Relation(sourceId);
			break;
		default:
			throw new AssertionError();
		}

		target.mergeFrom(source);
		targetDataSet.addPrimitive(target);

		// helps speed up the cleanUpReferencesAfterMerge() process.
		sourceTargetMap.push(source);
		sourceTargetMap.push(target);
		mergeMap.put(sourceId, sourceTargetMap);
	}

	private void cleanUpReferencesAfterMerge() {
		targetDataSet.getRelations().stream()
		.forEach(r -> setMembersOfRelationAfterMerge(r, targetDataSet));

		targetDataSet.getWays().stream()
		.forEach(w -> setNodesOfWayAfterMerge(w, targetDataSet));
	}

	private void setNodesOfWayAfterMerge(Way targetWay, DataSet targetDataSet) {
		Way sourceWay = (Way) getMergeSource(targetWay);

		List<Node> targetNodes = sourceWay.getNodes().stream()
				.map(n -> getMergeTarget(n))
				.map(p -> (Node) p)
				.collect(Collectors.toList());
		targetWay.setNodes(targetNodes);
	}

	private void setMembersOfRelationAfterMerge(Relation targetRel, DataSet targetDataSet) {
		Relation sourceRel = (Relation) getMergeSource(targetRel);

		List<RelationMember> targetMembers = sourceRel.getMembers().stream()
				.map(m -> new RelationMember(m.getRole(), getMergeTarget(m.getMember())))
				.collect(Collectors.toList());
		targetRel.setMembers(targetMembers);
	}

	private OsmPrimitive getMergeSource(OsmPrimitive targetPrimitive) {
		if (mergeMap.containsKey(targetPrimitive.getId())) {
			return mergeMap.get(targetPrimitive.getId()).getLast();
		}
		else {
			throw new IllegalStateException(tr("Merge map is missing a target for OsmPrimitive with id {0}", targetPrimitive.getUniqueId()));
		}
	}

	private OsmPrimitive getMergeTarget(OsmPrimitive sourcePrimitive) {
		if (mergeMap.containsKey(sourcePrimitive.getId())) {
			return mergeMap.get(sourcePrimitive.getId()).getFirst();
		}
		else {
			throw new IllegalStateException(tr("Merge map is missing a source for OsmPrimitive with id {0}", sourcePrimitive.getUniqueId()));
		}
	}

	private void mergeMetaData() {
		Area area = targetDataSet.getDataSourceArea();

		// copy the merged layer's data source info.
		// only add source rectangles if they are not contained in the layer already.
		for (DataSource sourceDataSource : sourceDataSet.getDataSources()) {
			if (area == null || !area.contains(sourceDataSource.bounds.asRect())) {
				targetDataSet.addDataSource(sourceDataSource);
			}
		}

		// copy the merged layer's API version.
		if (targetDataSet.getVersion() == null) {
			targetDataSet.setVersion(sourceDataSet.getVersion());
		}

		// copy the merged layer's policies and locked status.
		if (sourceDataSet.getUploadPolicy() != null && (targetDataSet.getUploadPolicy() == null
				|| sourceDataSet.getUploadPolicy().compareTo(targetDataSet.getUploadPolicy()) > 0)) {
			targetDataSet.setUploadPolicy(sourceDataSet.getUploadPolicy());
		}
		if (sourceDataSet.getDownloadPolicy() != null && (targetDataSet.getDownloadPolicy() == null
				|| sourceDataSet.getDownloadPolicy().compareTo(targetDataSet.getDownloadPolicy()) > 0)) {
			targetDataSet.setDownloadPolicy(sourceDataSet.getDownloadPolicy());
		}
		if (sourceDataSet.isLocked() && !targetDataSet.isLocked()) {
			targetDataSet.lock();
		}
	}

}
