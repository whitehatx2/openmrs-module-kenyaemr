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
package org.openmrs.module.kenyaemr.fragment.controller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.openmrs.Patient;
import org.openmrs.api.context.Context;
import org.openmrs.calculation.patient.PatientCalculationContext;
import org.openmrs.calculation.patient.PatientCalculationService;
import org.openmrs.calculation.result.CalculationResultMap;
import org.openmrs.calculation.result.ListResult;
import org.openmrs.module.kenyaemr.calculation.CalculationUtils;
import org.openmrs.module.kenyaemr.calculation.ScheduledVisitOnDayCalculation;
import org.openmrs.module.kenyaemr.calculation.VisitsOnDayCalculation;
import org.openmrs.module.kenyaemr.util.KenyaEmrUtils;
import org.openmrs.module.reporting.common.DateUtil;
import org.openmrs.ui.framework.SimpleObject;
import org.openmrs.ui.framework.annotation.FragmentParam;
import org.openmrs.ui.framework.fragment.FragmentModel;
import org.openmrs.util.PersonByNameComparator;

/**
 * Controller for daily schedule fragment
 */
public class DailyScheduleFragmentController {
	
	public void controller(FragmentModel model,
	                       @FragmentParam("page") String pageWhenClicked,
	                       @FragmentParam(value = "date", required = false) Date date) {

		Date today = KenyaEmrUtils.dateStartOfDay(new Date());
		Date tomorrow = KenyaEmrUtils.dateAddDays(today, 1);
		Date yesterday = KenyaEmrUtils.dateAddDays(today, -1);

		// Date defaults to today
		if (date == null) {
			date = today;
		}
		else {
			// Ignore time
			date = KenyaEmrUtils.dateStartOfDay(date);
		}

		// Run the calculations to get patients with scheduled visits
		PatientCalculationService cs = Context.getService(PatientCalculationService.class);
		Set<Integer> allPatients = Context.getPatientSetService().getAllPatients().getMemberIds();
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("date", date);
		PatientCalculationContext calcContext = cs.createCalculationContext();
		Set<Integer> scheduled = CalculationUtils.patientsThatPass(cs.evaluate(allPatients, new ScheduledVisitOnDayCalculation(), params, calcContext));
		CalculationResultMap actual = cs.evaluate(scheduled, new VisitsOnDayCalculation(), params, calcContext);

		// Sort patients and convert to simple objects
		List<Patient> scheduledPatients = Context.getPatientSetService().getPatients(scheduled);
		Collections.sort(scheduledPatients, new PersonByNameComparator());
		List<SimpleObject> list = new ArrayList<SimpleObject>();
		for (Patient p : scheduledPatients) {
			SimpleObject so = new SimpleObject();
			so.put("patient", p);
			so.put("visits", ((ListResult) actual.get(p.getPatientId())).getValues());
			list.add(so);
		}
		
		model.addAttribute("date", date);
		model.addAttribute("isToday", date.equals(today));
		model.addAttribute("isTomorrow", date.equals(tomorrow));
		model.addAttribute("isYesterday", date.equals(yesterday));
		model.addAttribute("scheduled", list);
	}
	
}
