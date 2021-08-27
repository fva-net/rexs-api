/*******************************************************************************
 * Copyright (C) 2020 FVA GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.  You may obtain a copy
 * of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/
package info.rexs.model;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.OptionalInt;
import java.util.Set;
import java.util.TimeZone;
import java.util.stream.Collectors;

import info.rexs.db.constants.RexsComponentType;
import info.rexs.db.constants.RexsRelationRole;
import info.rexs.db.constants.RexsRelationType;
import info.rexs.db.constants.RexsVersion;
import info.rexs.model.jaxb.Accumulation;
import info.rexs.model.jaxb.Component;
import info.rexs.model.jaxb.LoadSpectrum;
import info.rexs.model.jaxb.Model;
import info.rexs.model.jaxb.ObjectFactory;
import info.rexs.model.jaxb.Ref;
import info.rexs.model.jaxb.Relation;

/**
 * This class represents a REXS model.
 *
 * @author FVA GmbH
 */
public class RexsModel {

	/** Factory class to create new instances for the JAXB model. */
	protected ObjectFactory objectFactory = new ObjectFactory();

	/** The representation of this model in the JAXB model. */
	protected Model rawModel;

	/** All relations of the model as a {@link List} of {@link RexsRelation}. */
	protected List<RexsRelation> relations;

	/** All load spectrums of the model as a {@link List} of {@link RexsLoadSpectrum}. */
	protected List<RexsLoadSpectrum> loadSpectrums;

	/** An internal index with all components of the model for quick access. */
	private Map<Integer, RexsComponent> components;

	/** An internal index with all components of the component types in the model for quick access. */
	private Map<RexsComponentType, List<RexsComponent>> mapTypeToComponentId;

	/** An internal index with all relations of the component in the model for quick access. */
	private Map<Integer, List<RexsRelation>> mapMainCompToRelation;

	/** An internal index with all relations of the relation types in the model for quick access. */
	private Map<RexsRelationType, List<RexsRelation>> mapTypeToRelation;

	private RexsVersion version;

	/**
	 * Constructs a new {@link RexsModel} from scratch.
	 *
	 * @param applicationId
	 * 				Name of the application that created the REXS model, e.g. "FVA Workbench".
	 * @param applicationVersion
	 * 				Version of the application.
	 */
	protected RexsModel(String applicationId, String applicationVersion) {
		this.rawModel = createEmptyRexsModel(applicationId, applicationVersion);
		initialize();
	}

	/**
	 * Constructs a new {@link RexsModel} for the given {@link Model}.
	 *
	 * @param model
	 * 				The representation of this model in the JAXB model.
	 */
	protected RexsModel(Model model) {
		this.rawModel = model;
		initialize();
	}

	protected void initialize() {
		List<Component> rawComponents = rawModel.getComponents().getComponent();
		List<Relation> rawRelations = rawModel.getRelations().getRelation();

		this.version = RexsVersion.findByName(rawModel.getVersion());
		this.relations = new ArrayList<>();
		this.mapMainCompToRelation = new HashMap<>(rawRelations.size());
		this.mapTypeToRelation = new HashMap<>();

		for (Relation rawRelation : rawRelations) {
			RexsRelation relation = RexsModelObjectFactory.getInstance().createRexsRelation(rawRelation);
			this.relations.add(relation);

			List<RexsRelation> relationsOfComp = this.mapMainCompToRelation.get(relation.getMainComponentId());
			if (relationsOfComp == null)
				relationsOfComp = new ArrayList<>();
			relationsOfComp.add(relation);
			this.mapMainCompToRelation.put(relation.getMainComponentId(), relationsOfComp);

			List<RexsRelation> relationsOfType = mapTypeToRelation.get(relation.getType());
			if (relationsOfType==null)
				relationsOfType = new ArrayList<>();
			relationsOfType.add(relation);
			this.mapTypeToRelation.put(relation.getType(), relationsOfType);
		}

		this.components = new HashMap<>(rawComponents.size());
		this.mapTypeToComponentId = new HashMap<>();

		for (Component rawComponent : rawComponents) {
			RexsComponent component = RexsModelObjectFactory.getInstance().createRexsComponent(rawComponent);
			this.components.put(rawComponent.getId(), component);

			RexsComponentType componentType = RexsComponentType.findById(rawComponent.getType());
			List<RexsComponent> componentsOfType = this.mapTypeToComponentId.get(componentType);
			if (componentsOfType==null)
				componentsOfType = new ArrayList<>();
			componentsOfType.add(component);
			this.mapTypeToComponentId.put(componentType, componentsOfType);
		}

		this.loadSpectrums = new ArrayList<>();
		for (LoadSpectrum rawSpectrum : rawModel.getLoadSpectrum())
			this.loadSpectrums.add(RexsModelObjectFactory.getInstance().createRexsLoadSpectrum(rawSpectrum));
	}

	public RexsVersion getVersion() {
		return version;
	}

	private Model createEmptyRexsModel(String applicationId, String applicationVersion) {
		Model newRawModel = objectFactory.createModel();

		// set xml headers
		newRawModel.setVersion(RexsVersion.getLatest().getName());
		newRawModel.setDate(getISO8601Date());
		newRawModel.setApplicationId(applicationId);
		newRawModel.setApplicationVersion(applicationVersion);

		// leere Listen für Komponenten und Relationen anlegen
		newRawModel.setComponents(objectFactory.createComponents());
		newRawModel.setRelations(objectFactory.createRelations());

		return newRawModel;
	}

	private String getISO8601Date() {
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX");
		TimeZone tz = TimeZone.getDefault();
		df.setTimeZone(tz);
		return df.format(new Date());
	}

	/**
	 * @return
	 * 				The representation of this model in the JAXB model.
	 */
	public Model getRawModel() {
		return rawModel;
	}

	/**
	 * @return
	 * 				All components of the model as a {@link List} of {@link RexsComponent}.
	 */
	public Collection<RexsComponent> getComponents() {
		return components.values().stream().collect(Collectors.toList());
	}

	/**
	 * Checks on the ID of a component whether the model contains a corresponding component.
	 *
	 * @param compId
	 * 				The ID of the component as {@link Integer}.
	 *
	 * @return
	 * 				{@code true} if the model contains the component, otherwise {@code false}.
	 */
	public boolean hasComponent(Integer compId) {
		return components.containsKey(compId);
	}

	/**
	 * Returns the component of the sub-model for a numeric ID.
	 *
	 * @param compId
	 * 				The ID of the component as {@link Integer}.
	 *
	 * @return
	 * 				The component as {@link RexsComponent} or {@code null} if the sub-model does not contain a corresponding component.
	 */
	public RexsComponent getComponent(Integer compId) {
		return components.get(compId);
	}

	/**
	 * TODO Document me!
	 *
	 * @param mainCompId
	 * 				TODO Document me!
	 *
	 * @return
	 * 				TODO Document me!
	 */
	public List<RexsRelation> getRelationsOfMainComp(Integer mainCompId) {
		return mapMainCompToRelation.getOrDefault(mainCompId, Collections.emptyList());
	}

	/**
	 * TODO Document me!
	 *
	 * @param type
	 * 				TODO Document me!
	 *
	 * @return
	 * 				TODO Document me!
	 */
	public List<RexsRelation> getRelationsOfType(RexsRelationType type) {
		return mapTypeToRelation.getOrDefault(type, Collections.emptyList());
	}

	/**
	 * TODO Document me!
	 *
	 * @param componentType
	 * 				TODO Document me!
	 *
	 * @return
	 * 				TODO Document me!
	 */
	public List<RexsComponent> getComponentsOfType(RexsComponentType componentType) {
		return mapTypeToComponentId.getOrDefault(componentType, Collections.emptyList());
	}

	/**
	 * TODO Document me!
	 *
	 * @param compId
	 * 				TODO Document me!
	 *
	 * @return
	 * 				TODO Document me!
	 */
	public int getOrderOfAssemblyRelationOf(Integer compId) {
		List<RexsRelation> orderedAssemblyRelations = getRelationsOfType(RexsRelationType.ordered_assembly);
		for (RexsRelation relation : orderedAssemblyRelations) {
			if (relation.hasComponent(compId)
					&& relation.findRoleByComponentId(compId).equals(RexsRelationRole.part))
				return relation.getOrder();
		}

		return -1;
	}

	/**
	 * TODO Document me!
	 *
	 * @param compId
	 * 				TODO Document me!
	 *
	 * @return
	 * 				TODO Document me!
	 */
	public int getOrderOfReferenceRelationOf(Integer compId) {
		List<RexsRelation> orderedReferenceRelations = getRelationsOfType(RexsRelationType.ordered_reference);
		for (RexsRelation relation : orderedReferenceRelations) {
			if (relation.hasComponent(compId)
					&& relation.findRoleByComponentId(compId).equals(RexsRelationRole.referenced))
				return relation.getOrder();
		}

		return -1;
	}

	/**
	 * TODO Document me!
	 *
	 * @param compId
	 * 				TODO Document me!
	 * @param role
	 * 				TODO Document me!
	 *
	 * @return
	 * 				TODO Document me!
	 */
	public RexsRelation findFirstRelation(Integer compId, RexsRelationRole role) {
		for (RexsRelation relaltion : relations) {
			if (relaltion.hasComponent(compId)
					&& role.equals(relaltion.findRoleByComponentId(compId)))
				return relaltion;
		}
		return null;
	}

	/**
	 * TODO Document me!
	 *
	 * @param stageId
	 * 				TODO Document me!
	 *
	 * @return
	 * 				TODO Document me!
	 */
	public RexsRelation getStageRelationFromStageId(Integer stageId) {
		for (RexsRelation relation : getRelationsOfMainComp(stageId)) {
			if (relation.getType().equals(RexsRelationType.stage))
				return relation;
		}
		return null;
	}

	/**
	 * TODO Document me!
	 *
	 * @param stageId
	 * 				TODO Document me!
	 *
	 * @return
	 * 				TODO Document me!
	 */
	public RexsComponent getGear1OfStage(Integer stageId) {
		RexsRelation stageRel = getStageRelationFromStageId(stageId);
		return getComponent(stageRel.findComponentIdByRole(RexsRelationRole.gear_1));
	}

	/**
	 * TODO Document me!
	 *
	 * @param stageId
	 * 				TODO Document me!
	 *
	 * @return
	 * 				TODO Document me!
	 */
	public RexsComponent getGear2OfStage(Integer stageId) {
		RexsRelation stageRel = getStageRelationFromStageId(stageId);
		return getComponent(stageRel.findComponentIdByRole(RexsRelationRole.gear_2));
	}

//	/**
//	 * Finds all children of the desired type. If no such children exist, finds
//	 * all grand children of the desired type. If those do not exist either returns
//	 * an empty List.
//	 * Special case: if type==mainCompType returns mainComp
//	 * @param mainCompId
//	 * @param type
//	 * @return
//	 */
	/**
	 * TODO Document me!
	 *
	 * @param mainCompId
	 * 				TODO Document me!
	 * @param type
	 * 				TODO Document me!
	 *
	 * @return
	 * 				TODO Document me!
	 */
	public List<RexsComponent> getSubComponentsWithType(Integer mainCompId, RexsComponentType type) {
		RexsComponent mainComp = getComponent(mainCompId);
		if (mainComp.getType().equals(type))
			return Arrays.asList(mainComp);
		List<RexsComponent> subCompList = getChildrenWithType(mainCompId, type);
		if (subCompList.isEmpty())
			subCompList = getGrandChildrenWithType(mainCompId, type);
		return subCompList;
	}

	/**
	 * TODO Document me!
	 *
	 * @param mainCompId
	 * 				TODO Document me!
	 * @param type
	 * 				TODO Document me!
	 *
	 * @return
	 * 				TODO Document me!
	 */
	public List<RexsComponent> getChildrenWithType(Integer mainCompId, RexsComponentType type) {
		Set<RexsComponent> childrenWithType = new HashSet<>();
		for (RexsRelation relation : getRelationsOfMainComp(mainCompId)) {
			for (Integer subCompId : relation.getSubComponentIds()) {
				RexsComponent subComp = getComponent(subCompId);
				if (subComp.getType().equals(type))
					childrenWithType.add(subComp);
			}
		}
		if (type == RexsComponentType.stage_gear_data)
			childrenWithType.addAll(getAssociatedStageGearDataComponents(mainCompId));
		return childrenWithType.stream().sorted().collect(Collectors.toList());
	}

	/**
	 * TODO Document me!
	 *
	 * @param mainCompId
	 * 				TODO Document me!
	 * @param type
	 * 				TODO Document me!
	 *
	 * @return
	 * 				TODO Document me!
	 */
	private List<RexsComponent> getGrandChildrenWithType(Integer mainCompId, RexsComponentType type) {
		Set<RexsComponent> grandChildrenWithType = new HashSet<>();
		for (RexsRelation relation : getRelationsOfMainComp(mainCompId)) {
			for (Integer subCompId : relation.getSubComponentIds()) {
				grandChildrenWithType.addAll(getChildrenWithType(subCompId, type));
			}
		}
		return new ArrayList<>(grandChildrenWithType);
	}

	/**
	 * TODO Document me!
	 *
	 * @param compId
	 * 				TODO Document me!
	 *
	 * @return
	 * 				TODO Document me!
	 */
	public Set<RexsComponent> getAssociatedStageGearDataComponents(Integer compId) {
		Set<RexsComponent> stageGearDataComps = new HashSet<>();
		for (RexsRelation relation : getRelationsOfType(RexsRelationType.stage_gear_data)) {
			if (relation.hasComponent(compId)) {
				Integer stageGearDataId = relation.findComponentIdByRole(RexsRelationRole.stage_gear_data);
				RexsComponent stageGearData = getComponent(stageGearDataId);
				stageGearDataComps.add(stageGearData);
			}
		}
		return stageGearDataComps;
	}

	/**
	 * TODO Document me!
	 *
	 * @param stageGearDataId
	 * 				TODO Document me!
	 *
	 * @return
	 * 				TODO Document me!
	 */
	public RexsComponent getStageFromStageGearData(Integer stageGearDataId) {
		for (RexsRelation relation : getRelationsOfType(RexsRelationType.stage_gear_data)) {
			if (relation.hasComponent(stageGearDataId)) {
				Integer stageId = relation.findComponentIdByRole(RexsRelationRole.stage);
				return getComponent(stageId);
			}
		}
		return null;
	}

	/**
	 * TODO Document me!
	 *
	 * @param stageGearData
	 * 				TODO Document me!
	 *
	 * @return
	 * 				TODO Document me!
	 */
	public RexsComponent getGearFromStageGearData(RexsComponent stageGearData) {
		for (RexsRelation relation : getRelationsOfType(RexsRelationType.stage_gear_data)) {
			if (relation.hasComponent(stageGearData.getId())) {
				Integer gearId = relation.findComponentIdByRole(RexsRelationRole.gear);
				return getComponent(gearId);
			}
		}
		return null;
	}

	/**
	 * TODO Document me!
	 *
	 * @param stageId
	 * 				TODO Document me!
	 * @param gearId
	 * 				TODO Document me!
	 *
	 * @return
	 * 				TODO Document me!
	 */
	public RexsComponent getStageGearData(Integer stageId, Integer gearId) {
		for (RexsRelation relation : getRelationsOfType(RexsRelationType.stage_gear_data)) {
			if (relation.hasComponent(stageId) && relation.hasComponent(gearId)) {
				Integer stageGearDataId = relation.findComponentIdByRole(RexsRelationRole.stage_gear_data);
				return getComponent(stageGearDataId);
			}
		}
		return null;
	}

	/**
	 * TODO Document me!
	 *
	 * @param stageId
	 * 				TODO Document me!
	 *
	 * @return
	 * 				TODO Document me!
	 */
	public List<RexsComponent> getFlankGeometriesOfStage(Integer stageId) {
		List<RexsComponent> compList = new ArrayList<>();
		RexsRelation stageRel = getStageRelationFromStageId(stageId);
		Integer gear1Id = stageRel.findComponentIdByRole(RexsRelationRole.gear_1);
		Integer gear2Id = stageRel.findComponentIdByRole(RexsRelationRole.gear_2);

		compList.add(getFlankGeometry(gear1Id, RexsRelationRole.left.getKey()));
		compList.add(getFlankGeometry(gear1Id, RexsRelationRole.right.getKey()));
		compList.add(getFlankGeometry(gear2Id, RexsRelationRole.left.getKey()));
		compList.add(getFlankGeometry(gear2Id, RexsRelationRole.right.getKey()));

		return compList;
	}

//	/**
//	 * Liefert zu einem Rad die linek oder rechte Flanke
//	 */
	/**
	 * TODO Document me!
	 *
	 * @param gearId
	 * 				TODO Document me!
	 * @param flank
	 * 				TODO Document me!
	 *
	 * @return
	 * 				TODO Document me!
	 */
	public RexsComponent getFlankGeometry(Integer gearId, String flank) {
		RexsComponent flankGeometry = null;
		for (RexsRelation relation : getRelationsOfMainComp(gearId)) {
			if (relation.getType().equals(RexsRelationType.flank)) {
				RexsRelationRole role = RexsRelationRole.findByKey(flank);
				Integer flankId = relation.findComponentIdByRole(role);
				flankGeometry = getComponent(flankId);
				break;
			}
		}
		return flankGeometry;
	}

	/**
	 * TODO Document me!
	 *
	 * @return
	 * 				TODO Document me!
	 */
	public RexsComponent getGearUnit() {
		List<RexsComponent> listOfGearUnits = getComponentsOfType(RexsComponentType.gear_unit);
		if (listOfGearUnits.size() != 1)
			throw new RexsModelAccessException("there has to be exactly one gear_unit component in the model!");
		return listOfGearUnits.get(0);
	}

	/**
	 * TODO Document me!
	 *
	 * @param componentWithMaterial
	 * 				TODO Document me!
	 *
	 * @return
	 * 				TODO Document me!
	 */
	public RexsComponent getMaterial(RexsComponent componentWithMaterial) {
		List<RexsComponent> materials = getSubComponentsWithType(componentWithMaterial.getId(), RexsComponentType.material);
		if (materials == null || materials.isEmpty())
			return null;
		return materials.get(0);
	}

	/**
	 * TODO Document me!
	 *
	 * @param componentWithLubricant
	 * 				TODO Document me!
	 *
	 * @return
	 * 				TODO Document me!
	 */
	public RexsComponent getLubricant(RexsComponent componentWithLubricant) {
		List<RexsComponent> lubricants = getSubComponentsWithType(componentWithLubricant.getId(), RexsComponentType.lubricant);
		if (lubricants == null || lubricants.isEmpty())
			return null;
		return lubricants.get(0);
	}

	/**
	 * TODO Document me!
	 *
	 * @param type
	 * 				TODO Document me!
	 * @param id
	 * 				TODO Document me!
	 * @param name
	 * 				TODO Document me!
	 *
	 * @return
	 * 				TODO Document me!
	 */
	public RexsComponent createComponent(RexsComponentType type, Integer id, String name) {
		if (components.containsKey(id))
			throw new RexsModelAccessException("component with id " + id + " already exists");

		Component component = objectFactory.createComponent();
		component.setId(id);
		component.setType(type.getId());
		component.setName(name);

		RexsComponent rexsComponent = RexsModelObjectFactory.getInstance().createRexsComponent(component);

		components.put(id, rexsComponent);
		rawModel.getComponents().getComponent().add(component);

		List<RexsComponent> componentsForType =
				mapTypeToComponentId.containsKey(type) ?
						mapTypeToComponentId.get(type) : new ArrayList<>();
		componentsForType.add(rexsComponent);
		mapTypeToComponentId.put(type, componentsForType);

		return rexsComponent;
	}

	/**
	 * TODO Document me!
	 *
	 * @param type
	 * 				TODO Document me!
	 * @param name
	 * 				TODO Document me!
	 *
	 * @return
	 * 				TODO Document me!
	 */
	public RexsComponent createComponent(RexsComponentType type, String name) {
		Integer id = getNextFreeComponentId();
		return createComponent(type, id, name);
	}

	/**
	 * TODO Document me!
	 *
	 * @param relComp
	 * 				TODO Document me!
	 * @param firstPart
	 * 				TODO Document me!
	 * @param secondPart
	 * 				TODO Document me!
	 *
	 * @return
	 * 				TODO Document me!
	 */
	public boolean addCouplingRelation(RexsComponent relComp, RexsComponent firstPart, RexsComponent secondPart) {
		if (!componentsExists(relComp, firstPart, secondPart))
			return false;

		Relation rexsRelation = createRexsRelationWithType(RexsRelationType.coupling);

		rexsRelation.getRef().add(createRexsRefWithType(toRelationData(relComp), RexsRelationRole.assembly));
		rexsRelation.getRef().add(createRexsRefWithType(toRelationData(firstPart), RexsRelationRole.side_1));
		rexsRelation.getRef().add(createRexsRefWithType(toRelationData(secondPart), RexsRelationRole.side_2));

		addRelation(rexsRelation);
		return true;
	}

	/**
	 * TODO Document me!
	 *
	 * @param relComp
	 * 				TODO Document me!
	 * @param innerPart
	 * 				TODO Document me!
	 * @param outerPart
	 * 				TODO Document me!
	 *
	 * @return
	 * 				TODO Document me!
	 */
	public boolean addSideRelation(RexsComponent relComp, RexsComponent innerPart, RexsComponent outerPart) {
		if (!componentsExists(relComp, innerPart, outerPart))
			return false;

		Relation rexsRelation = createRexsRelationWithType(RexsRelationType.side);

		rexsRelation.getRef().add(createRexsRefWithType(toRelationData(relComp), RexsRelationRole.assembly));
		rexsRelation.getRef().add(createRexsRefWithType(toRelationData(innerPart), RexsRelationRole.inner_part));
		rexsRelation.getRef().add(createRexsRefWithType(toRelationData(outerPart), RexsRelationRole.outer_part));

		addRelation(rexsRelation);
		return true;
	}

	/**
	 * TODO Document me!
	 *
	 * @param side1
	 * 				TODO Document me!
	 * @param side2
	 * 				TODO Document me!
	 *
	 * @return
	 * 				TODO Document me!
	 */
	public boolean addConnectionRelation(RexsComponent side1, RexsComponent side2) {
		if (!componentsExists(side1, side2))
			return false;

		Relation rexsRelation = createRexsRelationWithType(RexsRelationType.connection);

		rexsRelation.getRef().add(createRexsRefWithType(toRelationData(side1), RexsRelationRole.side_1));
		rexsRelation.getRef().add(createRexsRefWithType(toRelationData(side2), RexsRelationRole.side_2));

		addRelation(rexsRelation);
		return true;
	}

	/**
	 * TODO Document me!
	 *
	 * @param stage
	 * 				TODO Document me!
	 * @param gear1
	 * 				TODO Document me!
	 * @param gear2
	 * 				TODO Document me!
	 *
	 * @return
	 * 				TODO Document me!
	 */
	public boolean addStageRelation(RexsComponent stage, RexsComponent gear1, RexsComponent gear2) {
		if (!componentsExists(stage, gear1, gear2))
			return false;

		Relation rexsRelation = createRexsRelationWithType(RexsRelationType.stage);

		rexsRelation.getRef().add(createRexsRefWithType(toRelationData(stage), RexsRelationRole.stage));
		rexsRelation.getRef().add(createRexsRefWithType(toRelationData(gear1), RexsRelationRole.gear_1));
		rexsRelation.getRef().add(createRexsRefWithType(toRelationData(gear2), RexsRelationRole.gear_2));

		addRelation(rexsRelation);
		return true;
	}

	/**
	 * TODO Document me!
	 *
	 * @param stage
	 * 				TODO Document me!
	 * @param gear
	 * 				TODO Document me!
	 * @param stageGearData
	 * 				TODO Document me!
	 *
	 * @return
	 * 				TODO Document me!
	 */
	public boolean addStageGearDataRelation(RexsComponent stage, RexsComponent gear, RexsComponent stageGearData) {
		if (!componentsExists(stage, gear, stageGearData))
			return false;

		Relation rexsRelation = createRexsRelationWithType(RexsRelationType.stage_gear_data);

		rexsRelation.getRef().add(createRexsRefWithType(toRelationData(stage), RexsRelationRole.stage));
		rexsRelation.getRef().add(createRexsRefWithType(toRelationData(gear), RexsRelationRole.gear));
		rexsRelation.getRef().add(createRexsRefWithType(toRelationData(stageGearData), RexsRelationRole.stage_gear_data));

		addRelation(rexsRelation);
		return true;
	}

	/**
	 * TODO Document me!
	 *
	 * @param mainComp
	 * 				TODO Document me!
	 * @param partComp
	 * 				TODO Document me!
	 *
	 * @return
	 * 				TODO Document me!
	 */
	public boolean addAssemblyRelation(RexsComponent mainComp, RexsComponent partComp) {
		if (!componentsExists(mainComp, partComp))
			return false;

		Relation rexsRelation = createRexsRelationWithType(RexsRelationType.assembly);

		rexsRelation.getRef().add(createRexsRefWithType(toRelationData(mainComp), RexsRelationRole.assembly));
		rexsRelation.getRef().add(createRexsRefWithType(toRelationData(partComp), RexsRelationRole.part));

		addRelation(rexsRelation);
		return true;
	}

	/**
	 * TODO Document me!
	 *
	 * @param mainComp
	 * 				TODO Document me!
	 * @param referenced
	 * 				TODO Document me!
	 *
	 * @return
	 * 				TODO Document me!
	 */
	public boolean addReferenceRelation(RexsComponent mainComp, RexsComponent referenced) {
		if (!componentsExists(mainComp, referenced))
			return false;

		Relation rexsRelation = createRexsRelationWithType(RexsRelationType.reference);

		rexsRelation.getRef().add(createRexsRefWithType(toRelationData(mainComp), RexsRelationRole.origin));
		rexsRelation.getRef().add(createRexsRefWithType(toRelationData(referenced), RexsRelationRole.referenced));

		addRelation(rexsRelation);
		return true;
	}

	/**
	 * TODO Document me!
	 *
	 * @param mainComp
	 * 				TODO Document me!
	 * @param referenced
	 * 				TODO Document me!
	 * @param order
	 * 				TODO Document me!
	 *
	 * @return
	 * 				TODO Document me!
	 */
	public boolean addOrderedReferenceRelation(RexsComponent mainComp, RexsComponent referenced, int order) {
		if (!componentsExists(mainComp, referenced))
			return false;

		Relation rexsRelation = createRexsRelationWithType(RexsRelationType.ordered_reference);
		rexsRelation.setOrder(order);

		rexsRelation.getRef().add(createRexsRefWithType(toRelationData(mainComp), RexsRelationRole.origin));
		rexsRelation.getRef().add(createRexsRefWithType(toRelationData(referenced), RexsRelationRole.referenced));

		addRelation(rexsRelation);
		return true;
	}

	/**
	 * TODO Document me!
	 *
	 * @param gear
	 * 				TODO Document me!
	 * @param flank1
	 * 				TODO Document me!
	 * @param flank2
	 * 				TODO Document me!
	 *
	 * @return
	 * 				TODO Document me!
	 */
	public boolean addFlankRelation(RexsComponent gear, RexsComponent flank1, RexsComponent flank2) {
		if (!componentsExists(gear, flank1, flank2))
			return false;

		Relation rexsRelation = createRexsRelationWithType(RexsRelationType.flank);

		rexsRelation.getRef().add(createRexsRefWithType(toRelationData(gear), RexsRelationRole.gear));
		rexsRelation.getRef().add(createRexsRefWithType(toRelationData(flank1), RexsRelationRole.left));
		rexsRelation.getRef().add(createRexsRefWithType(toRelationData(flank2), RexsRelationRole.right));

		addRelation(rexsRelation);
		return true;
	}

	/**
	 * TODO Document me!
	 *
	 * @param mainComp
	 * 				TODO Document me!
	 * @param partComp
	 * 				TODO Document me!
	 * @param order
	 * 				TODO Document me!
	 *
	 * @return
	 * 				TODO Document me!
	 */
	public boolean addOrderedAssemblyRelation(RexsComponent mainComp, RexsComponent partComp, int order) {
		if (!componentsExists(mainComp, partComp))
			return false;

		Relation rexsRelation = createRexsRelationWithType(RexsRelationType.ordered_assembly);
		rexsRelation.setOrder(order);

		rexsRelation.getRef().add(createRexsRefWithType(toRelationData(mainComp), RexsRelationRole.assembly));
		rexsRelation.getRef().add(createRexsRefWithType(toRelationData(partComp), RexsRelationRole.part));

		addRelation(rexsRelation);
		return true;
	}

	/**
	 * TODO Document me!
	 *
	 * @param flank
	 * 				TODO Document me!
	 * @param tool
	 * 				TODO Document me!
	 * @param manufacturingStep
	 * 				TODO Document me!
	 *
	 * @return
	 * 				TODO Document me!
	 */
	public boolean addManufacturingStepRelation(RexsComponent flank, RexsComponent tool, RexsComponent manufacturingStep, int order) {
		if (!componentsExists(flank, tool, manufacturingStep))
			return false;

		Relation rexsRelation = createRexsRelationWithType(RexsRelationType.manufacturing_step);
		rexsRelation.setOrder(order);

		rexsRelation.getRef().add(createRexsRefWithType(toRelationData(flank), RexsRelationRole.workpiece));
		rexsRelation.getRef().add(createRexsRefWithType(toRelationData(tool), RexsRelationRole.tool));
		rexsRelation.getRef().add(createRexsRefWithType(toRelationData(manufacturingStep), RexsRelationRole.manufacturing_settings));

		addRelation(rexsRelation);
		return true;
	}

	private Relation createRexsRelationWithType(RexsRelationType type) {
		Relation rexsRelation = objectFactory.createRelation();
		rexsRelation.setId(getNextFreeRelationId());
		rexsRelation.setType(type.getKey());
		return rexsRelation;
	}

	private Ref createRexsRefWithType(RexsRelationData data, RexsRelationRole role) {
		Ref ref = objectFactory.createRef();
		ref.setId(data.getId());
		ref.setRole(role.getKey());
		ref.setHint(data.getHint());
		return ref;
	}

	private RexsRelationData toRelationData(RexsComponent rexsComponent) {
		return RexsModelObjectFactory.getInstance().createRexsRelationData(rexsComponent.getId(), rexsComponent.getType().getId());
	}

	private void addRelation(Relation relation) {
		RexsRelation rexsRelation = RexsModelObjectFactory.getInstance().createRexsRelation(relation);

		relations.add(rexsRelation);
		rawModel.getRelations().getRelation().add(relation);

		List<RexsRelation> relationsForType =
				mapTypeToRelation.containsKey(rexsRelation.getType()) ?
						mapTypeToRelation.get(rexsRelation.getType()) : new ArrayList<>();
		relationsForType.add(rexsRelation);
		mapTypeToRelation.put(rexsRelation.getType(), relationsForType);

		List<RexsRelation> relationsForMainComp =
				mapMainCompToRelation.containsKey(rexsRelation.getMainComponentId()) ?
						mapMainCompToRelation.get(rexsRelation.getMainComponentId()) : new ArrayList<>();
		relationsForMainComp.add(rexsRelation);
		mapMainCompToRelation.put(rexsRelation.getMainComponentId(), relationsForMainComp);
	}

	protected boolean componentsExists(RexsComponent ... rexsComponents) {
		if (rexsComponents == null)
			return false;

		for (RexsComponent rexsComponent : rexsComponents) {
			if (!hasComponent(rexsComponent.getId()))
				return false;
		}
		return true;
	}

	/**
	 * TODO Document me!
	 *
	 * @param rexsComponent
	 * 				TODO Document me!
	 *
	 * @return
	 * 				TODO Document me!
	 */
	public boolean isPlanetPin(RexsComponent rexsComponent) {
		for (RexsRelation planetPinRel : getRelationsOfType(RexsRelationType.planet_pin)) {
			Integer shaftId = planetPinRel.findComponentIdByRole(RexsRelationRole.shaft);
			if (rexsComponent.getId().equals(shaftId))
				return true;
		}
		return false;
	}

	/**
	 * TODO Document me!
	 *
	 * @param rexsComponent
	 * 				TODO Document me!
	 *
	 * @return
	 * 				TODO Document me!
	 */
	public boolean isPlanetShaft(RexsComponent rexsComponent) {
		for (RexsRelation planetShaftRel : getRelationsOfType(RexsRelationType.planet_shaft)) {
			Integer shaftId = planetShaftRel.findComponentIdByRole(RexsRelationRole.shaft);
			if (rexsComponent.getId().equals(shaftId))
				return true;
		}
		return false;
	}

	/**
	 * TODO Document me!
	 *
	 * @param rexsComponent
	 * 				TODO Document me!
	 *
	 * @return
	 * 				TODO Document me!
	 */
	public boolean isPartOfPlanetaryStage(RexsComponent rexsComponent) {
		Integer componentId = rexsComponent.getId();
		RexsComponentType componentType = rexsComponent.getType();

		if (RexsComponentType.cylindrical_stage.getId().equals(componentType.getId())
				|| RexsComponentType.shaft.getId().equals(componentType.getId())) {
			for (RexsComponent planetaryStage : getComponentsOfType(RexsComponentType.planetary_stage)) {
				for (RexsRelation relation : getRelationsOfMainComp(planetaryStage.getId())) {
					if (componentId.equals(relation.findComponentIdByRole(RexsRelationRole.part)))
						return true;
				}
			}
			return false;

		} else if (RexsComponentType.cylindrical_gear.getId().equals(componentType.getId())
				|| RexsComponentType.ring_gear.getId().equals(componentType.getId())) {
			for (RexsComponent stage : getStagesOfGear(rexsComponent)) {
				if (isPartOfPlanetaryStage(stage))
					return true;
			}
			return false;

		} else if (RexsComponentType.side_plate.getId().equals(componentType.getId())
				|| RexsComponentType.planet_carrier.getId().equals(componentType.getId())) {
			return true;

		} else if (RexsComponentType.concept_bearing.getId().equals(componentType.getId())
				|| RexsComponentType.coupling.getId().equals(componentType.getId())
				|| RexsComponentType.rolling_bearing_with_catalog_geometry.getId().equals(componentType.getId())
				|| RexsComponentType.rolling_bearing_with_detailed_geometry.getId().equals(componentType.getId())) {
			for (RexsRelation relation : getRelationsOfMainComp(rexsComponent.getId())) {
				if (relation.getType()==RexsRelationType.side) {
					RexsComponent sideComp1 = getComponent(relation.findComponentIdByRole(RexsRelationRole.side_1));
					RexsComponent sideComp2 = getComponent(relation.findComponentIdByRole(RexsRelationRole.side_2));
					if (isPartOfPlanetaryStage(sideComp1) && isPartOfPlanetaryStage(sideComp2))
						return true;
				} else {
					return false;
				}
			}
		}
		return false;
	}

	/**
	 * TODO Document me!
	 *
	 * @param gear
	 * 				TODO Document me!
	 *
	 * @return
	 * 				TODO Document me!
	 */
	public List<RexsComponent> getStagesOfGear(RexsComponent gear) {
		RexsComponentType gearType = gear.getType();
		RexsComponentType stageType = null;

		if (RexsComponentType.cylindrical_gear.getId().equals(gearType.getId())
				|| RexsComponentType.ring_gear.getId().equals(gearType.getId())) {
			stageType = RexsComponentType.cylindrical_stage;
		} else if (RexsComponentType.bevel_gear.getId().equals(gearType.getId())) {
			stageType = RexsComponentType.bevel_stage;
		}

		if (stageType == null)
			return Collections.emptyList();

		List<RexsComponent> stages = new ArrayList<>();
		for (RexsComponent stage : getComponentsOfType(stageType)) {
			if (getSubComponentsWithType(stage.getId(), gear.getType()).contains(gear))
				stages.add(stage);
		}
		return stages;
	}

	/**
	 * TODO Document me!
	 *
	 * @param subCompId
	 * 				TODO Document me!
	 * @param typeOfParent
	 * 				TODO Document me!
	 *
	 * @return
	 * 				TODO Document me!
	 */
	public RexsComponent getParent(Integer subCompId, RexsComponentType typeOfParent) {
		RexsComponent parent = null;
		for (RexsComponent potentialParent : getComponentsOfType(typeOfParent)) {
			for (RexsRelation relation : getRelationsOfMainComp(potentialParent.getId())) {
				if (relation.getType().equals(RexsRelationType.assembly)
						&& subCompId.equals(relation.findComponentIdByRole(RexsRelationRole.part))) {
					if (parent == null)
						parent = potentialParent;
					else
						return null; // non-unique parent!
				}
			}
		}
		return parent;
	}

	/**
	 * TODO Document me!
	 *
	 * @param subCompId
	 * 				TODO Document me!
	 * @param typeOfParent
	 * 				TODO Document me!
	 *
	 * @return
	 * 				TODO Document me!
	 */
	public boolean hasParentOfType(Integer subCompId, RexsComponentType typeOfParent) {
		return getParent(subCompId, typeOfParent) != null;
	}

	private Integer getNextFreeComponentId() {
		OptionalInt max = components.keySet().stream().mapToInt(Integer::intValue).max();
		if (max.isPresent())
			return max.getAsInt() + 1;
		return 1;
	}

	private Integer getNextFreeRelationId() {
		OptionalInt max = relations.stream().mapToInt(relation -> relation.getId().intValue()).max();
		if (max.isPresent())
			return max.getAsInt() + 1;
		return 1;
	}

	/**
	 * TODO Document me!
	 *
	 * @param gear
	 * 				TODO Document me!
	 *
	 * @return
	 * 				TODO Document me!
	 */
	public RexsComponent getFinishingToolOfGear(RexsComponent gear) {
		List<RexsRelation> relationsOfGear = getRelationsOfMainComp(gear.getId());
		int highestOrder = 0;
		RexsComponent finishingTool = null;
		for (RexsRelation relation : relationsOfGear) {
			if (relation.getType() == RexsRelationType.ordered_reference) {
				int order = relation.getOrder();
				if (order >= highestOrder) {
					highestOrder = order;
					finishingTool = getComponent(relation.findComponentIdByRole(RexsRelationRole.referenced));
				}
			}
		}
		return finishingTool;
	}

	/**
	 * TODO Document me!
	 *
	 * @param gear
	 * 				TODO Document me!
	 *
	 * @return
	 * 				TODO Document me!
	 */
	public RexsComponent getShaftOfGear(RexsComponent gear) {
		return getParent(gear.getId(), RexsComponentType.shaft);
	}

	/**
	 * TODO Document me!
	 *
	 * @return
	 * 				TODO Document me!
	 */
	public List<RexsSubModel> getLoadCases() {
		if (loadSpectrums.isEmpty())
			return new ArrayList<>();

		List<RexsSubModel> loadCases = loadSpectrums.get(0).getLoadCases();
		Collections.sort(loadCases);
		return loadCases;
	}

	/**
	 * TODO Document me!
	 *
	 * @return
	 * 				TODO Document me!
	 */
	public RexsSubModel getAccumulation() {
		if (loadSpectrums.isEmpty()) {
			Accumulation empty = null;
			return RexsModelObjectFactory.getInstance().createRexsSubModel(empty);
		}
		return loadSpectrums.get(0).getAccumulation();
	}

	/**
	 * Changes the numerical ID of a component.
	 * <p>
	 * ATTENTION: Make sure that the new ID has not already been assigned to another component.
	 *
	 * @param component
	 * 				The component of the REXS model whose ID is to be changed.
	 * @param newId
	 * 				The new numeric ID of the component within the REXS model.
	 */
	public void changeComponentId(RexsComponent component, Integer newId) {
		Integer oldId = component.getId();
		component.changeComponentId(newId);

		components.remove(oldId);
		components.put(newId, component);

		for (RexsRelation relation : relations) {
			if (relation.hasComponent(oldId))
				relation.changeComponentId(oldId, newId);
		}

		List<RexsRelation> mainrelations = mapMainCompToRelation.get(oldId);
		mapMainCompToRelation.put(newId, mainrelations);
		mapMainCompToRelation.remove(oldId);

		for (RexsLoadSpectrum loadSpectrum : loadSpectrums) {
			for (RexsSubModel subModel : loadSpectrum.getLoadCases()) {
				subModel.changeComponentId(oldId, newId);
			}
			loadSpectrum.getAccumulation().changeComponentId(oldId, newId);
		}
	}

	/**
	 * TODO Document me!
	 *
	 * @param component
	 * 				TODO Document me!
	 */
	public void changeComponentId(RexsComponent component) {
		changeComponentId(component, getNextFreeComponentId());
	}

	/**
	 * TODO Document me!
	 *
	 * @param subModel
	 * 				TODO Document me!
	 */
	public void copyAttributesFromSubModelToMaster(RexsSubModel subModel) {
		for (RexsComponent masterComp : this.getComponents()) {
			if (!subModel.hasComponent(masterComp.getId()))
				continue;
			RexsComponent subModelComp = subModel.getComponent(masterComp.getId());
			for (RexsAttribute attribute : subModelComp.getAttributes()) {
				masterComp.addAttribute(attribute);
			}
		}
	}
}
