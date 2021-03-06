package org.cryptocoinpartners.command;

import org.cryptocoinpartners.report.AdHocJpaReport;
import org.cryptocoinpartners.report.Report;
import org.cryptocoinpartners.report.TableOutput;


/**
 * @author Tim Olson
 */
@CommandName("jpa")
@SuppressWarnings("UnusedDeclaration")
public class JpaReportCommand extends ReportCommand {

    @Override
    public String getUsageHelp() {
        return "jpa {jpa_query}";
    }

    @Override
    public String getExtraHelp() {
        return "Runs the specified ad-hoc query against the database and prints the result as a table";
    }


    @Override
    public void parse(String commandArguments) {
        queryStr = commandArguments;
    }

    @Override
    protected Report getReport() {
        AdHocJpaReport jpaReport = injector.getInstance(AdHocJpaReport.class);
        jpaReport.setQueryString(queryStr);
        return jpaReport;
    }


    @Override
    public void run() {
        super.run();
    }

    @Override
    protected TableOutput runReport(Report report) {
        try {
            return super.runReport(report);
        }
        catch( IllegalArgumentException e ) {
            out.println(e.getMessage());
            return null;
        }
    }


    private String queryStr;
}
