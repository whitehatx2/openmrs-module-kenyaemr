/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */
package org.openmrs.module.kenyaemr.regimen;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.openmrs.Concept;
import org.openmrs.DrugOrder;
import org.openmrs.api.context.Context;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

/**
 * Manager for regimens
 */
public class RegimenManager {

	private static Map<String, Map<String, Integer>> drugConcepts = new LinkedHashMap<String, Map<String, Integer>>();

	private static Map<String, List<RegimenDefinition>> regimenDefinitions = new HashMap<String, List<RegimenDefinition>>();

	private static int definitionsVersion = 0;

	/**
	 * Gets the category codes
	 * @return the category codes
	 */
	public static Set<String> getCategoryCodes() {
		return drugConcepts.keySet();
	}

	/**
	 * Gets the individual drug concepts for the given category
	 * @param category the category, e.g. "ARV"
	 * @return the concept ids
	 */
	public static Map<String, Integer> getDrugConcepts(String category) {
		return drugConcepts.get(category);
	}

	/**
	 * Gets the regimen definitions for the given category
	 * @param category the category, e.g. "ARV"
	 * @return the regimen definitions
	 */
	public static List<RegimenDefinition> getRegimenDefinitions(String category) {
		return regimenDefinitions.get(category);
	}

	/**
	 * Gets the version number of the definitions (from XML)
	 * @return the version number
	 */
	public static int getDefinitionsVersion() {
		return definitionsVersion;
	}

	/**
	 * Looks up the drug code for the given concept in the given category
	 * @param category the category, e.g. "ARV"
	 * @param concept the drug concept
	 * @return the drug code
	 */
	public static String findDrugCode(String category, Concept concept) {
		Map<String, Integer> concepts = drugConcepts.get(category);
		if (concepts == null) {
			throw new IllegalArgumentException("No such regimen category: " + category);
		}

		for (Map.Entry<String, Integer> entry : concepts.entrySet()) {
			if (entry.getValue().equals(concept.getConceptId())) {
				return entry.getKey();
			}
		}
		return null;
	}

	/**
	 * Finds definitions that match the given regimen
	 * @param category the category, e.g. "ARV"
	 * @param regimen the regimen
	 * @param exact whether matches must be exact (includes dose, units and frequency)
	 * @return the definitions
	 */
	public static List<RegimenDefinition> findDefinitions(String category, Regimen regimen, boolean exact) {
		List<RegimenDefinition> definitions = regimenDefinitions.get(category);
		if (definitions == null) {
			throw new IllegalArgumentException("No such regimen category: " + category);
		}

		List<RegimenDefinition> matches = new ArrayList<RegimenDefinition>();

		outer:
		for (RegimenDefinition definition : definitions) {
			List<RegimenDefinition.RegimenComponent> components = definition.getComponents();
			Set<DrugOrder> orders = regimen.getDrugOrders();

			// Skip if regimen doesn't have same number of orders
			if (components.size() != orders.size()) {
				continue;
			}

			// Check each component has an equivalent drug order
			for (RegimenDefinition.RegimenComponent component : components) {

				// Does regimen have a drug order for this component?
				boolean regimenHasComponent = false;
				for (DrugOrder order : orders) {
					if (order.getConcept().getConceptId().equals(component.getConceptId())) {

						if (!exact || (ObjectUtils.equals(order.getDose(), component.getDose()) && StringUtils.equals(order.getUnits(), component.getUnits()) && StringUtils.equals(order.getFrequency(), component.getFrequency()))) {
							regimenHasComponent = true;
							break;
						}
					}
				}

				if (!regimenHasComponent) {
					continue outer;
				}
			}

			// Regimen has all components of the definition
			matches.add(definition);
		}

		return matches;
	}

	/**
	 * Loads definitions from an input stream containing XML
	 * @param stream the path to XML resource
	 * @throws ParserConfigurationException
	 * @throws IOException
	 * @throws SAXException
	 */
	public static void loadDefinitionsFromXML(InputStream stream) throws ParserConfigurationException, IOException, SAXException {
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = dbFactory.newDocumentBuilder();

		Document document = builder.parse(stream);

		Element root = document.getDocumentElement();
		definitionsVersion = Integer.parseInt(root.getAttribute("version"));

		drugConcepts.clear();
		regimenDefinitions.clear();

		// Parse each category
		NodeList categoryNodes = root.getElementsByTagName("category");
		for (int c = 0; c < categoryNodes.getLength(); c++) {
			Element categoryElement = (Element)categoryNodes.item(c);
			String categoryCode = categoryElement.getAttribute("code");

			Map<String, Integer> drugs = new HashMap<String, Integer>();
			List<RegimenDefinition> regimens = new ArrayList<RegimenDefinition>();

			// Parse all drug concepts for this category
			NodeList drugNodes = categoryElement.getElementsByTagName("drug");
			for (int d = 0; d < drugNodes.getLength(); d++) {
				Element drugElement = (Element)drugNodes.item(d);
				String drugCode = drugElement.getAttribute("code");
				String drugConceptUuid = drugElement.getAttribute("conceptUuid");

				Concept drugConcept = Context.getConceptService().getConceptByUuid(drugConceptUuid);

				drugs.put(drugCode, drugConcept.getConceptId());
			}

			// Parse all groups for this category
			NodeList groupNodes = categoryElement.getElementsByTagName("group");
			for (int g = 0; g < groupNodes.getLength(); g++) {
				Element groupElement = (Element)groupNodes.item(g);
				String groupCode = groupElement.getAttribute("code");

				// Parse all regimen definitions for this group
				NodeList regimenNodes = groupElement.getElementsByTagName("regimen");
				for (int r = 0; r < regimenNodes.getLength(); r++) {
					Element regimenElement = (Element)regimenNodes.item(r);
					String name = regimenElement.getAttribute("name");

					RegimenDefinition regimenDefinition = new RegimenDefinition(name, groupCode);

					// Parse all components for this regimen
					NodeList componentNodes = regimenElement.getElementsByTagName("component");
					for (int p = 0; p < componentNodes.getLength(); p++) {
						Element componentElement = (Element)componentNodes.item(p);
						String drugCode = componentElement.getAttribute("drugCode");
						Double dose = componentElement.hasAttribute("dose") ? Double.parseDouble(componentElement.getAttribute("dose")) : null;
						String units = componentElement.hasAttribute("units") ? componentElement.getAttribute("units") : null;
						String frequency = componentElement.hasAttribute("frequency") ? componentElement.getAttribute("frequency") : null;

						Integer drugConceptId = drugs.get(drugCode);
						if (drugConceptId == null)
							throw new RuntimeException("Regimen component references invalid drug: " + drugCode);

						regimenDefinition.addComponent(drugConceptId, dose, units, frequency);
					}

					regimens.add(regimenDefinition);
				}
			}

			drugConcepts.put(categoryCode, drugs);
			regimenDefinitions.put(categoryCode, regimens);
		}
	}
}