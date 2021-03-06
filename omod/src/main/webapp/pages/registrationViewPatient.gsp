<%
	ui.decorateWith("kenyaemr", "standardKenyaEmrPage", [ patient: patient ])

	ui.includeCss("kenyaemr", "kenyaemr.css");
%>

<style type="text/css">
	fieldset {
		margin-bottom: 0.6em;
	}
	
	.active-visit-tab {
		border: 1px black solid;
		border-top-left-radius: 0.5em;
		border-bottom-left-radius: 0.5em;
		margin-bottom: 0.6em;
		padding: 0.3em;
		position: relative;
		right: -5px;
		z-index: 1;
	}
	
	.active-visit-tab h4 {
		margin: 0.3em;
	}
	
	.selected-visit-tab {
		background-color: #ffffbb;
		border-right: none;
	}

	.selectable:hover {
		cursor: pointer;
		background-color: #e0e0e0;
	}
		
</style>

<div id="patient-col1">
	${ ui.includeFragment("kenyaemr", "patientOverallDetails", [
			patient: patient,
			visit: visit,
			activeVisits: activeVisits
		]) }
</div>

<div id="patient-col2" <% if (visit) { %>class="selected-visit-tab"<% } %>>

<% /* Bill wants this, but I really don't think it fits. Consider putting in gray bar instead
	<div style="float: right">
		${ ui.includeFragment("uilibrary", "widget/button", [
				iconProvider: "uilibrary",
				icon: "user_search_32.png",
				classes: [ "padded" ]
			]) }
	</div>
*/ %>

	<% if (visit) { %>

		<% if (!visit.stopDatetime) { %>
			<div style="float: right">
				<%= ui.includeFragment("uilibrary", "widget/popupForm", [
					id: "check-out-form",
					buttonConfig: [
						label: "End Visit",
						/* classes: [ "padded" ], */
						extra: "patient going home",
						iconProvider: "kenyaemr",
						icon: "buttons/visit_end.png"
					],
					/* dialogOpts: """{ open: function() { jq('#check-out-form input[type=submit]').focus(); } }""", */
					popupTitle: "Check Out",
					fields: [
						[ hiddenInputName: "visit.visitId", value: visit.visitId ],
						[ label: "End Date and Time", formFieldName: "visit.stopDatetime", class: java.util.Date, initialValue: new Date(), fieldFragment: "field/java.util.Date.datetime" ]
					],
					fragment: "registrationUtil",
					fragmentProvider: "kenyaemr",
					action: "editVisit",
					successCallbacks: [ "location.href = '${ ui.pageLink("kenyaemr", "registrationViewPatient", [ patientId: patient.id ]) }'" ],
					submitLabel: ui.message("general.submit"),
					cancelLabel: ui.message("general.cancel"),
					submitLoadingMessage: "Checking Out"
				]) %>
			</div>
		<% } %>
		
		<h4>Current ${ ui.format(visit.visitType) } Visit</h4>

		${ ui.includeFragment("kenyaemr", "availableForms", [ visit: visit ]) }
		
	<% } else {
		// do this here to avoid annoying template engine issue
		def jsSuccess = "location.href = ui.pageLink('kenyaemr', 'registrationViewPatient', " + "{" + "patientId: ${ patient.id }, visitId: data.visitId" + "});"
	%>
	
		<h4>No current visit</h4>

		<%= ui.includeFragment("uilibrary", "widget/popupForm", [
				id: "check-in-form",
				buttonConfig: [
					iconProvider: "uilibrary",
					icon: "user_add_32.png",
					label: "Check In For Visit",
					classes: [ "padded" ],
					extra: "Patient is Here"
				],
				popupTitle: "Check In For Visit",
				prefix: "visit",
				commandObject: newCurrentVisit,
				hiddenProperties: [ "patient" ],
				properties: [ "visitType", "startDatetime" ],
				fieldConfig: [
					"visitType": [ label: "Visit Type" ]
				],
				propConfig: [
					"visitType": [ type: "radio" ],
				],
				fieldConfig: [
					"startDatetime": [ fieldFragment: "field/java.util.Date.datetime" ]
				],
				fragment: "registrationUtil",
				fragmentProvider: "kenyaemr",
				action: "startVisit",
				successCallbacks: [ jsSuccess ],
				submitLabel: ui.message("general.submit"),
				cancelLabel: ui.message("general.cancel"),
				submitLoadingMessage: "Checking In"
			]) %>
	<% } %>
	
	<br/>

</div>

<% if (visit) { %>
	
	${ ui.includeFragment("kenyaemr", "showHtmlForm", [ id: "showHtmlForm", style: "display: none" ]) }
	
	${ ui.includeFragment("kenyaemr", "dialogSupport") }

<% } %>