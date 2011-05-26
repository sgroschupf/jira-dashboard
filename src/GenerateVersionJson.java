import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;

public class GenerateVersionJson {
    private static final String CATEGORIES_PLACEHOLDER = "%CATEGORIES_PLACEHOLDER%";
    private static final String DATA_PLACEHOLDER = "%DATA_PLACEHOLDER%";
    private static final String NOW_PLACEHOLDER = "%NOW_PLACEHOLDER%";

    private static String DAP_PROJECT_KEY = "DAP";
    private static String[] ISSUE_STATES = new String[] { "open", "in progress", "reopened", "resolved", "closed" };
    private static String[] STATE_COLORS = new String[] { "#A7432C", "#AF7324", "#FFE716", "#7BAF00", "#1398AB" };

    public static void main(String[] args) throws IOException, ParseException, InterruptedException {
	GenerateVersionJson generator = new GenerateVersionJson();
	if (args.length != 5) {
	    System.out.println("usage: jiraUrl, jiraProjectKey, jiraUserName jiraPassword targetFile");
	    System.exit(-1);
	}

	String jiraUrl = args[0];
	String jiraProjectKey = args[1];
	String userName = args[2];
	String password = args[3];
	String targetFile = args[4];

	while (true) {
	    generator.generateJsonFile(jiraUrl, jiraProjectKey, userName, password, targetFile);
	    long numMillisecondsToSleep = 1000 * 60 * 5;
	    Thread.sleep(numMillisecondsToSleep);
	}
    }

    private void generateJsonFile(String jiraUrl, String projectKey, String userName, String password, String targetFile) throws IOException, ParseException {

	String JIRA_DAP_PROJECT = jiraUrl + "/rest/api/latest/project/" + projectKey + "?";
	String JIRA_SEARCH = jiraUrl + "/rest/api/latest/search?jql=";

	String authSuffix = "os_username=" + userName + "&os_password=" + password;

	StringBuffer response = loadDataFromUrl(JIRA_DAP_PROJECT + authSuffix);
	HashMap<String, HashMap<String, Object>> projectVersions = parseVersionsFromJson(response);

	String[] versionNames = projectVersions.keySet().toArray(new String[projectVersions.size()]);
	Arrays.sort(versionNames);

	for (String name : versionNames) {
	    HashMap<String, Object> version = projectVersions.get(name);

	    for (String status : ISSUE_STATES) {
		String issuesForVersionQueryString = buildQueryString(name, status);

		StringBuffer issueForVersionJson = loadDataFromUrl(JIRA_SEARCH + issuesForVersionQueryString + "&" + authSuffix);
		long count = parseCount(issueForVersionJson);
		version.put(status, count);
		// if (count > 0)
		// System.out.println(name + ":" + status + ":" + count);
	    }
	}

	// categories
	StringBuffer versionSnippet = new StringBuffer("[");
	for (String version : versionNames) {

	    String releaseDate = (String) projectVersions.get(version).get("releaseDate");

	    versionSnippet.append("'" + version + "( " + releaseDate + " )',");
	}
	versionSnippet = removeLastComma(versionSnippet);
	versionSnippet.append("]");

	// data
	StringBuffer dataSnippet = new StringBuffer();

	for (int i = 0; i < ISSUE_STATES.length; i++) {
	    String status = ISSUE_STATES[i];

	    String issueColor = STATE_COLORS[i];

	    dataSnippet.append("{");
	    dataSnippet.append("name: '" + status + "',");
	    dataSnippet.append("color: '" + issueColor + "',");
	    dataSnippet.append("data: [");
	    for (String versionName : versionNames) {
		HashMap<String, Object> version = projectVersions.get(versionName);
		Long count = (Long) version.get(status);
		dataSnippet.append(count + ",");
	    }
	    // remove last comma
	    dataSnippet = removeLastComma(dataSnippet);
	    dataSnippet.append("]");
	    dataSnippet.append("},");
	}
	dataSnippet = removeLastComma(dataSnippet);

	String dataString = dataSnippet.toString().replace("''", "'");
	String template = readTemplate();
	template = template.replace(NOW_PLACEHOLDER, new SimpleDateFormat("MM/dd/yy - HH:mm z").format(new Date()));
	template = template.replace(CATEGORIES_PLACEHOLDER, versionSnippet);
	template = template.replace(DATA_PLACEHOLDER, dataString);

	FileOutputStream outputStream = new FileOutputStream(targetFile);
	outputStream.write(template.getBytes());
	outputStream.flush();
	outputStream.close();
	System.out.println("generated new json file.");
    }

    private String buildQueryString(String name, String status) {
	String issuesForVersionQueryString = URLEncoder.encode("project = " + DAP_PROJECT_KEY + " AND fixVersion = ") + "'" + name + "'"
		+ URLEncoder.encode(" AND status ='" + status + "'");
	return issuesForVersionQueryString;
    }

    private String readTemplate() throws IOException {
	StringBuffer buffer = new StringBuffer();
	BufferedReader in = new BufferedReader(new FileReader("./chart_teamplate.json"));
	String str;
	while ((str = in.readLine()) != null) {
	    buffer.append(str);
	}
	in.close();
	return buffer.toString();
    }

    private StringBuffer removeLastComma(StringBuffer categories) {
	return categories.deleteCharAt(categories.length() - 1);
    }

    private long parseCount(StringBuffer sb) {
	JSONObject jsonObject = (JSONObject) JSONSerializer.toJSON(sb.toString());
	return jsonObject.getLong("total");
    }

    private StringBuffer loadDataFromUrl(String urlString) throws IOException {
	BufferedReader in = new BufferedReader(new InputStreamReader(new URL(urlString).openStream()));
	StringBuffer sb = new StringBuffer();
	String line = null;
	while ((line = in.readLine()) != null) {
	    sb.append(line);
	}
	return sb;
    }

    private HashMap<String, HashMap<String, Object>> parseVersionsFromJson(StringBuffer sb) throws ParseException {
	JSONObject json = (JSONObject) JSONSerializer.toJSON(sb.toString());
	JSONArray versions = json.getJSONArray("versions");

	HashMap<String, HashMap<String, Object>> versionsMap = new HashMap<String, HashMap<String, Object>>();
	for (int i = 0; i < versions.size(); i++) {
	    JSONObject version = (JSONObject) versions.get(i);
	    // we only interested in version that have a release date...
	    if (version.has("releaseDate") && !version.getBoolean("released")) {
		String name = version.getString("name");
		String releaseDate = version.getString("releaseDate");
		HashMap<String, Object> hashMap = new HashMap<String, Object>();
		Date parsedDate = parseDate(releaseDate);
		String dateStr = new SimpleDateFormat("MM/dd").format(parsedDate);
		hashMap.put("releaseDate", dateStr);
		versionsMap.put(name, hashMap);

	    }
	}
	return versionsMap;
    }

    private Date parseDate(String releaseDate) throws ParseException {
	DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
	return (Date) formatter.parse(releaseDate);
    }
}
