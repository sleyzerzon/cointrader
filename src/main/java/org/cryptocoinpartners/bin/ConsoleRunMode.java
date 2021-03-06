package org.cryptocoinpartners.bin;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jline.Terminal;
import jline.TerminalFactory;
import jline.console.ConsoleReader;
import jline.console.KeyMap;
import jline.console.history.MemoryHistory;

import org.apache.commons.lang.StringUtils;
import org.cryptocoinpartners.command.Command;
import org.cryptocoinpartners.command.CommandBase;
import org.cryptocoinpartners.command.ConsoleWriter;
import org.cryptocoinpartners.command.ParseError;
import org.cryptocoinpartners.enumeration.TransactionType;
import org.cryptocoinpartners.module.BasicPortfolioService;
import org.cryptocoinpartners.module.BasicQuoteService;
import org.cryptocoinpartners.module.Context;
import org.cryptocoinpartners.module.MockOrderService;
import org.cryptocoinpartners.module.xchange.XchangeAccountService;
import org.cryptocoinpartners.module.xchange.XchangeData;
import org.cryptocoinpartners.module.xchange.XchangeOrderService;
import org.cryptocoinpartners.schema.Amount;
import org.cryptocoinpartners.schema.Currencies;
import org.cryptocoinpartners.schema.DiscreteAmount;
import org.cryptocoinpartners.schema.Exchanges;
import org.cryptocoinpartners.schema.Owner;
import org.cryptocoinpartners.schema.Portfolio;
import org.cryptocoinpartners.schema.Transaction;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;

/**
 * @author Tim Olson
 */
@SuppressWarnings("UnusedDeclaration")
@Parameters(commandNames = { "console", "terminal" }, commandDescription = "run in interactive mode")
public class ConsoleRunMode extends RunMode {

    @Parameter(names = "--live", description = "Enables LIVE trading mode (NOT RECOMMENDED)")
    public boolean live = false;

    @Override
    public void run() {
        try {
            init();
            //noinspection InfiniteLoopStatement
            while (true) {
                console.println();
                String line = console.readLine();
                if (StringUtils.isEmpty(line))
                    continue;
                Matcher matcher = Pattern.compile("^(\\w+)\\s*(.*)$").matcher(line);
                if (!matcher.matches()) {
                    out.println("Could not understand command name");
                    continue;
                }
                String commandName = matcher.group(1);
                Command command;
                try {
                    command = CommandBase.commandForName(commandName, context);
                } catch (Throwable e) {
                    log.warn("Could not create command " + commandName, e);
                    internalError();
                    continue;
                }
                if (command == null) {
                    out.println("Unknown command " + commandName + ".  Available commands:");
                    out.printList(CommandBase.allCommandNames());
                    continue;
                }
                String argStr = matcher.group(2);
                if (argStr == null)
                    argStr = "";
                try {
                    command.parse(argStr);
                } catch (ParseError e) {
                    out.println(e.getMessage());
                    String usageHelp = command.getUsageHelp();
                    out.println(usageHelp == null ? commandName : usageHelp);
                    continue;
                } catch (Throwable e) {
                    log.warn("Could not parse command " + commandName, e);
                    continue;
                }
                try {
                    command.run();
                } catch (Throwable e) {
                    log.warn("Could not run command " + commandName, e);
                    internalError();
                    continue;
                }
                history.add(line);
            }
        } catch (IOException e) {
            throw new Error("Console exception", e);
        }
    }

    private void internalError() {
        out.println("Internal error: see cointrader.log");
    }

    private void init() throws IOException {
        context = Context.create();
        context.attach(XchangeData.class);
        context.attach(BasicQuoteService.class);
        context.attach(XchangeAccountService.class);
        if (live)
            context.attach(XchangeOrderService.class);
        else
            context.attach(MockOrderService.class);

        Owner owner = new Owner("Console Portfolio");
        Portfolio portfolio = owner.getPortfolio();
        // Adjust the Owner's Portfolio to have some BTC & USD to play with.
        Amount amount = new DiscreteAmount(10000000, Currencies.BTC.getBasis());
        Amount price = new DiscreteAmount(0, Currencies.BTC.getBasis());
        Transaction initialCredit = new Transaction(portfolio, Exchanges.BITFINEX, Currencies.BTC, TransactionType.CREDIT, amount, price);
        context.publish(initialCredit);

        context.attachInstance(owner);

        Terminal terminal = TerminalFactory.get();
        try {
            terminal.init();
        } catch (Exception e) {
            throw new Error("Could not initialize terminal", e);
        }
        terminal.setEchoEnabled(false);

        console = new ConsoleReader();
        String prompt = config.getString("console.cursor", "ct>") + " ";
        console.setPrompt(prompt);
        history = new MemoryHistory();
        history.setMaxSize(config.getInt("console.hisory.size", 100));
        console.setHistory(history);
        console.setHistoryEnabled(true);
        console.setKeyMap(KeyMap.EMACS);
        out = new ConsoleWriter(console);
        context.attach(ConsoleWriter.class, out);
        context.attach(PrintWriter.class, out);
        context.attach(ConsoleNotifications.class);
        console.println();
        console.println("Coin Trader Console " + config.getString("project.version"));
        if (live)
            console.println("-= LIVE TRADING MODE =-");
    }

    private Context context;
    private ConsoleReader console;
    private ConsoleWriter out;
    private MemoryHistory history;
}
