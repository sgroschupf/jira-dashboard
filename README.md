Jira-Dashboard
--------------
This tool generates a widget for geckboard showing the process of your jira project by version.

<img src="https://github.com/sgroschupf/jira-dashboard/raw/master/screens/jira-widget.png" alt="Screenshot jira dashboard" />

Technically this tool just generates a json file configuring a highchart stacked bar chart that can be loaded into geckoboard.
The file needs to be placed on a server that is accessible via http. 
A free dropbox account is a simple hack to get to a free webserver for static files.


For more information see:
http://support.geckoboard.com/entries/274940-custom-chart-widget-type-definitions
http://www.highcharts.com/


Installation
-------
1.) Get a free dropbox account and install the app on the computer that you will run this tool.
2.) Dropbox will mount as drive. Identify the path of the dropbox public folder.
3.) run the tool:
java -jar jira-dashboard-05-22-2010.jar http://jira.datameer.com JiraProjectKey jiraUserName jiraPassword /path/to/dropboxMount/public/jira.json
The tool will re generate the jira.json file every 5 minutes. The first run will take a few minutes, wait until you see a log message "generated new json file."
4.) Login into dropbox website, navigate to the public folder and get the public url of the generated file.
<img src="https://github.com/sgroschupf/jira-dashboard/raw/master/screens/dropBox.png" alt="Screenshot dropbox" />
5.) In geckoboard click add widget, choose Custom Chart, choose highrise chart. 
6.) Paste the public URL from dropbox of the jira.json file into the URL data feed field. Choose a reload time and give the widget a name like "jira chart".
7. That's it.

P.S. I just hacked that together on a late night - so don't judge the code - be happy there is something. :) 