<%
	def moduleVersionFull = "v" + moduleVersion
	if (moduleBuildDate)
		moduleVersionFull += " (" + ui.format(moduleBuildDate) + ")"
%>

<div id="page-header">
	<div style="float: left">
		<a href="/${ contextPath }/index.htm?<% if (config.context) { %>${ config.context }<% } %>">
			<img src="${ ui.resourceLink("uilibrary", "images/openmrs_logo_white.gif") }" width="50" height="50"/>
		</a>
	</div>
	<div style="float: left">
		<span style="font-size: 1.5em;">Kenya EMR</span>
		<span style="font-size: 0.6em;">
			${ moduleVersionFull }, powered by <a style="color: #000; text-decoration: none; border-bottom: 1px dotted #999" href="http://openmrs.org">OpenMRS</a>
		</span>
		<br/>
		<% if (systemLocation) { %>
			<span style="font-weight: bold; margin-left: 0.5em; border-top: 1px gray solid;">${ ui.format(systemLocation) }</span>
		<% } %>
	</div>

	<div style="float: right; text-align: right">
		<img src="${ ui.resourceLink("kenyaemr", "images/moh_logo.png") }"/>
	</div>
	<div style="float: right; text-align: right; font-size: 0.9em; padding-right: 5px">
		Government of Kenya<br/>
		Ministry of Medical Services<br/>
		Ministry of Public Health and Sanitation
	</div>

	<div style="clear: both"></div>
</div>