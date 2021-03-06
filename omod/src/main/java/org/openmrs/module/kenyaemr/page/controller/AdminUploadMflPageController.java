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
package org.openmrs.module.kenyaemr.page.controller;

import org.openmrs.module.kenyaemr.util.MflLocationImporter;
import org.openmrs.ui.framework.page.PageModel;
import org.springframework.web.bind.annotation.RequestParam;


/**
 *
 */
public class AdminUploadMflPageController {
	
	public void controller(@RequestParam(required = false, value = "csv") String csv,
	                       PageModel model) throws Exception {
		Integer numSaved = null;
		if (csv != null) {
			numSaved = new MflLocationImporter().importCsv(csv);
		}
		model.addAttribute("numSaved", numSaved);
	}
	
}
