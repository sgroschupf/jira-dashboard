Jira-Dashboard
--------------

<img src="https://github.com/sgroschupf/jira-dashboard/raw/master/screens/jira-widget.png" alt="Screenshot" />

Generates a highchart.com stacked bar chart json file that can be loaded into a geckboard.
The file needs to be placed on a server that is accessable via http. 
A simple free solution for this is the public folder in a drobbox account.


For more information see:
http://support.geckoboard.com/entries/274940-custom-chart-widget-type-definitions
http://www.highcharts.com/

To run
java -jar jira-dashboard-05-22-2010.jar http://jira.datameer.com JiraProjectKey jiraUserName jiraPassword /path/to/drobboxMount/public/jira.json