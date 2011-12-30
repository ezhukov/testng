package org.testng.reporters.jq;

import org.testng.ISuite;
import org.testng.ITestNGMethod;
import org.testng.ITestResult;
import org.testng.collections.Maps;
import org.testng.reporters.XMLStringBuffer;

import java.util.List;
import java.util.Map;

public class TimesPanel extends BaseMultiSuitePanel {
  private Map<String, Long> m_totalTime = Maps.newHashMap();

  public TimesPanel(Model model) {
    super(model);
  }

  private static String getTag(ISuite suite) {
    return "times-" + suiteToTag(suite);
  }

  @Override
  public String getHeader(ISuite suite) {
    return "Times for " + suite.getName();
  }

  @Override
  public String getPanelName(ISuite suite) {
    return getTag(suite);
  }

  private String js(ISuite suite) {
    String functionName = "tableData_" + suiteToTag(suite);
    StringBuilder result = new StringBuilder(
        "suiteTableInitFunctions.push('" + functionName + "');\n"
          + "function " + functionName + "() {\n"
          + "var data = new google.visualization.DataTable();\n"
          + "data.addColumn('number', 'Number');\n"
          + "data.addColumn('string', 'Method');\n"
          + "data.addColumn('string', 'Class');\n"
          + "data.addColumn('number', 'Time (ms)');\n");

    List<ITestResult> allTestResults = getModel().getAllTestResults(suite);
    result.append(
      "data.addRows(" + allTestResults.size() + ");\n");

    int index = 0;
    for (ITestResult tr : allTestResults) {
      ITestNGMethod m = tr.getMethod();
      long time = tr.getEndMillis() - tr.getStartMillis();
      result
          .append("data.setCell(" + index + ", "
              + "0, " + index + ")\n")
          .append("data.setCell(" + index + ", "
              + "1, '" + m.getMethodName() + "')\n")
          .append("data.setCell(" + index + ", "
              + "2, '" + m.getTestClass().getName() + "')\n")
          .append("data.setCell(" + index + ", "
              + "3, " + time + ");\n");
      Long total = m_totalTime.get(suite.getName());
      if (total == null) {
        total = 0L;
      }
      m_totalTime.put(suite.getName(), total + time);
      index++;
    }

    result.append(
        "window.suiteTableData['" + suiteToTag(suite) + "']" +
        		"= { tableData: data, tableDiv: 'times-div-" + suiteToTag(suite) + "'}\n"
        + "return data;\n" +
        "}\n");

    return result.toString();
  }

  @Override
  public String getContent(ISuite suite, XMLStringBuffer main) {
    XMLStringBuffer xsb = new XMLStringBuffer(main.getCurrentIndent());
    xsb.push(D, C, "times-div");
    xsb.push("script", "type", "text/javascript");
    xsb.addString(js(suite));
    xsb.pop("script");
    xsb.addRequired(S, String.format("Total running time: %s",
        prettyDuration(m_totalTime.get(suite.getName()))),
        C, "suite-total-time");
    xsb.push(D, "id", "times-div-" + suiteToTag(suite));
    xsb.pop(D);
    xsb.pop(D);
    return xsb.toXML();
  }

  private String prettyDuration(long totalTime) {
    String result;
    if (totalTime < 1000) {
      result = totalTime + " ms";
    } else if (totalTime < 1000 * 60) {
      result = (totalTime / 1000) + " seconds";
    } else if (totalTime < 1000 * 60 * 60) {
      result = (totalTime / 1000 / 60) + " minutes";
    } else {
      result = (totalTime / 1000 / 60 / 60) + " hours";
    }
    return result;
  }
}
