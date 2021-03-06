package org.cryptocoinpartners.command;

import org.antlr.v4.runtime.misc.NotNull;

import javax.inject.Inject;


/**
 * @author Tim Olson
 */
@SuppressWarnings("UnusedDeclaration")
public class CsvArgsListener extends CsvBaseListener {

    @Override
    public void exitStartDate(@NotNull CsvParser.StartDateContext ctx) {
        command.startDate = ctx.getText();
    }


    @Override
    public void exitTickDuration(@NotNull CsvParser.TickDurationContext ctx) {
        command.endDate = ctx.getText();
    }


    @Override
    public void exitFilename(@NotNull CsvParser.FilenameContext ctx) {
        command.filename = ctx.getText();
    }


    @Inject
    private CsvCommand command;
}
