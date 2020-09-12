package hu.bme.mit.theta.solver.smtlib.cli;

import com.beust.jcommander.IParameterValidator;
import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import com.beust.jcommander.Parameters;
import hu.bme.mit.theta.common.Tuple2;
import hu.bme.mit.theta.common.logging.ConsoleLogger;
import hu.bme.mit.theta.common.logging.Logger;
import hu.bme.mit.theta.solver.smtlib.SmtLibSolverInstallerException;
import hu.bme.mit.theta.solver.smtlib.manager.SmtLibSolverManager;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class SmtLibCli {
    private static final String JAR_NAME = "theta-solver-smtlib-cli.jar";
    private final String[] args;

    private Logger logger;

    static class MainParams {
        @Parameter(names = "--home", description = "The path of the solver registry")
        String home = Path.of(System.getProperty("user.home"), ".theta").toAbsolutePath().toString();

        @Parameter(names = "--loglevel", description = "Detailedness of logging")
        Logger.Level logLevel = Logger.Level.MAINSTEP;

        @Parameter(names = "--help", help = true)
        private boolean help = false;
    }

    @Parameters(commandDescription = "Installs the solver")
    static class InstallCommand {
        static final String COMMAND = "install";

        @Parameter(description = "The solver to install (<solver_name>:<solver_version>)", validateWith = SolverNameAndVersionValidator.class, required = true)
        String solver;

        @Parameter(names = "--name", description = "Install the solver version under this custom name (<solver_name>:<name>), instead of the default (<solver_name>:<solver_version>)")
        String name;
    }

    @Parameters(commandDescription = "Installs a generic solver")
    static class InstallGenericCommand {
        static final String COMMAND = "install-generic";

        @Parameter(names = "--solverPath", description = "The path of the generic solver to install", required = true)
        String solverPath;

        @Parameter(names = "--solverArgs", description = "The arguments of the generic solver to invoke with")
        String solverArgs;

        @Parameter(names = "--name", description = "Install the solver version under this custom name (<solver_name>:<name>), instead of the default (<solver_name>:<solver_version>)", required = true)
        String name;
    }

    @Parameters(commandDescription = "Uninstalls the solver")
    static class UninstallCommand {
        static final String COMMAND = "uninstall";

        @Parameter(description = "The solver to uninstall (<solver_name>:<solver_version>)", validateWith = SolverNameAndVersionValidator.class, required = true)
        String solver;
    }

    @Parameters(commandDescription = "Reinstalls the solver")
    static class ReinstallCommand {
        static final String COMMAND = "reinstall";

        @Parameter(description = "The solver to reinstall (<solver_name>:<solver_version>)", validateWith = SolverNameAndVersionValidator.class, required = true)
        String solver;
    }

    @Parameters(commandDescription = "Prints info about the solver")
    static class GetInfoCommand {
        static final String COMMAND = "get-info";

        @Parameter(description = "The solver to print info about (<solver_name>:<solver_version>)", validateWith = SolverNameAndVersionValidator.class, required = true)
        String solver;
    }

    @Parameters(commandDescription = "Edits the runtime arguments passed to the solver")
    static class EditArgsCommand {
        static final String COMMAND = "edit-args";

        @Parameter(description = "The solver, whose runtime arguments are to be edited (<solver_name>:<solver_version>)", validateWith = SolverNameAndVersionValidator.class, required = true)
        String solver;

        @Parameter(names = "--print", description = "Print the path instead of opening it for editing")
        boolean print = false;
    }

    @Parameters(commandDescription = "Lists installed solvers and their versions")
    static class ListInstalledCommand {
        static final String COMMAND = "list-installed";

        @Parameter(description = "The solver, whose installed versions are to be listed (<solver_name>)", validateWith = SolverNameValidator.class)
        String solver;
    }

    @Parameters(commandDescription = "Lists supported solvers and their versions")
    static class ListSupportedCommand {
        static final String COMMAND = "list-supported";

        @Parameter(description = "The solver, whose supported versions are to be listed (<solver_name>)", validateWith = SolverNameValidator.class)
        String solver;
    }

    public SmtLibCli(final String[] args) {
        this.args = args;
    }

    public static void main(final String[] args) {
        final SmtLibCli mainApp = new SmtLibCli(args);
        mainApp.run();
    }

    private void run() {
        final var mainParams = new MainParams();
        final var installCommand = new InstallCommand();
        final var installGenericCommand = new InstallGenericCommand();
        final var uninstallCommand = new UninstallCommand();
        final var reinstallCommand = new ReinstallCommand();
        final var getInfoCommand = new GetInfoCommand();
        final var editArgsCommand = new EditArgsCommand();
        final var listInstalledCommand = new ListInstalledCommand();
        final var listSupportedCommand = new ListSupportedCommand();

        final var jc = JCommander.newBuilder()
            .addObject(mainParams)
            .addCommand(InstallCommand.COMMAND, installCommand)
            .addCommand(InstallGenericCommand.COMMAND, installGenericCommand)
            .addCommand(UninstallCommand.COMMAND, uninstallCommand)
            .addCommand(ReinstallCommand.COMMAND, reinstallCommand)
            .addCommand(GetInfoCommand.COMMAND, getInfoCommand)
            .addCommand(EditArgsCommand.COMMAND, editArgsCommand)
            .addCommand(ListInstalledCommand.COMMAND, listInstalledCommand)
            .addCommand(ListSupportedCommand.COMMAND, listSupportedCommand)
            .programName(JAR_NAME)
            .build();

        try {
            jc.parse(args);
            logger = new ConsoleLogger(mainParams.logLevel);
        } catch (final ParameterException ex) {
            System.out.println("Invalid parameters, details:");
            System.out.println(ex.getMessage());
            ex.usage();
            return;
        }

        if(mainParams.help) {
            jc.usage();
            return;
        }

        try {
            final var homePath = createIfNotExists(Path.of(mainParams.home));
            final var smtLibSolverManager = SmtLibSolverManager.create(homePath, logger);

            switch(jc.getParsedCommand()) {
                case InstallCommand.COMMAND: {
                    final var solver = decodeVersionString(installCommand.solver, smtLibSolverManager);

                    if(solver.get1().equals(smtLibSolverManager.getGenericInstallerName())) {
                        logger.write(Logger.Level.RESULT, "To install a generic solver, use the \"%s\" command", InstallGenericCommand.COMMAND);
                        return;
                    }

                    if(installCommand.name != null) {
                        smtLibSolverManager.install(solver.get1(), solver.get2(), installCommand.name);
                    }
                    else {
                        smtLibSolverManager.install(solver.get1(), solver.get2(), solver.get2());
                    }

                    return;
                }
                case InstallGenericCommand.COMMAND: {
                    smtLibSolverManager.installGeneric(
                        installGenericCommand.name,
                        Path.of(installGenericCommand.solverPath),
                        (installGenericCommand.solverArgs == null ? "" : installGenericCommand.solverArgs).split(" ")
                    );
                    return;
                }
                case UninstallCommand.COMMAND: {
                    final var solver = decodeVersionString(uninstallCommand.solver, smtLibSolverManager);
                    smtLibSolverManager.uninstall(solver.get1(), solver.get2());
                    return;
                }
                case ReinstallCommand.COMMAND: {
                    final var solver = decodeVersionString(reinstallCommand.solver, smtLibSolverManager);
                    smtLibSolverManager.reinstall(solver.get1(), solver.get2());
                    return;
                }
                case GetInfoCommand.COMMAND: {
                    final var solver = decodeVersionString(getInfoCommand.solver, smtLibSolverManager);
                    final var info = smtLibSolverManager.getInfo(solver.get1(), solver.get2());
                    logger.write(Logger.Level.RESULT, "%s\n", info);
                    return;
                }
                case EditArgsCommand.COMMAND: {
                    final var solver = decodeVersionString(editArgsCommand.solver, smtLibSolverManager);
                    final var argsFilePath = smtLibSolverManager.getArgsFile(solver.get1(), solver.get2());

                    if(smtLibSolverManager.getSupportedVersions(solver.get1()).contains(solver.get2())) {
                        logger.write(Logger.Level.RESULT, "Supported versions of solvers cannot be edited. If you want to pass custom arguments to a supported solver, install a new instance of the version with a custom name.");
                        return;
                    }

                    boolean nanoInPath = Stream.of(System.getenv("PATH").split(Pattern.quote(File.pathSeparator)))
                        .map(Paths::get)
                        .anyMatch(path -> Files.exists(path.resolve("nano")));

                    if(editArgsCommand.print) {
                        logger.write(Logger.Level.RESULT, String.format("%s\n", argsFilePath.toAbsolutePath().toString()));
                    }
                    else if(Desktop.isDesktopSupported()) {
                        Desktop.getDesktop().edit(argsFilePath.toFile());
                    }
                    else if(nanoInPath) {
                        Runtime.getRuntime().exec("nano", new String[] { argsFilePath.toAbsolutePath().toString() });
                    }
                    else {
                        logger.write(Logger.Level.MAINSTEP, "Open the following text file in your favourite editor, and edit the content:\n");
                        logger.write(Logger.Level.RESULT, String.format("%s\n", argsFilePath.toAbsolutePath().toString()));
                    }

                    return;
                }
                case ListInstalledCommand.COMMAND: {
                    if(listInstalledCommand.solver != null) {
                        logger.write(Logger.Level.MAINSTEP, "The currently installed versions of solver %s are: \n", listInstalledCommand.solver);
                        smtLibSolverManager.getInstalledVersions(listInstalledCommand.solver).forEach(version -> {
                            logger.write(Logger.Level.RESULT, "\t%s:%s\n", listInstalledCommand.solver, version);
                        });
                    }
                    else {
                        logger.write(Logger.Level.MAINSTEP, "The currently installed solvers are: \n");
                        smtLibSolverManager.getInstalledVersions().forEach(solver -> {
                            solver.get2().forEach(version -> {
                                logger.write(Logger.Level.RESULT, "\t%s:%s\n", solver.get1(), version);
                            });
                        });
                    }
                    return;
                }
                case ListSupportedCommand.COMMAND: {
                    if(listSupportedCommand.solver != null) {
                        logger.write(Logger.Level.MAINSTEP, "The currently supported versions of solver %s are: \n", listSupportedCommand.solver);
                        smtLibSolverManager.getSupportedVersions(listSupportedCommand.solver).forEach(version -> {
                            logger.write(Logger.Level.RESULT, "\t%s:%s\n", listSupportedCommand.solver, version);
                        });
                    }
                    else {
                        logger.write(Logger.Level.MAINSTEP, "The currently supported solvers are: \n");
                        smtLibSolverManager.getSupportedVersions().forEach(solver -> {
                            solver.get2().forEach(version -> {
                                logger.write(Logger.Level.RESULT, "\t%s:%s\n", solver.get1(), version);
                            });
                        });
                    }
                    return;
                }
                default: {
                    logger.write(Logger.Level.RESULT, "Unknown command\n");
                    jc.usage();
                    return;
                }
            }
        } catch (SmtLibSolverInstallerException | IOException e) {
            printError(e);
        }
    }

    private static Tuple2<String, String> decodeVersionString(final String version, final SmtLibSolverManager solverManager) throws SmtLibSolverInstallerException {
        final var versionArr = version.split(":");

        if(versionArr.length != 2) {
            throw new IllegalArgumentException("Invalid version string: " + version);
        }

        final var solver = versionArr[0];
        final var ver = versionArr[1];
        if(!ver.equals("latest")) {
            return Tuple2.of(solver, versionArr[1]);
        }
        else {
            final var versions = solverManager.getSupportedVersions(solver);
            if(versions.size() > 0) {
                return Tuple2.of(solver, versions.get(0));
            }
            else {
                throw new IllegalArgumentException("There are no supported versions of solver: " + solver);
            }
        }
    }

    private Path createIfNotExists(final Path path) throws IOException {
        if(!Files.exists(path)) {
            Files.createDirectory(path);
        }
        return path;
    }

    private void printError(final Throwable ex) {
        final String message = ex.getMessage() == null ? "" : ": " + ex.getMessage();
        logger.write(Logger.Level.RESULT, "Exception of type %s occurred%n", ex.getClass().getSimpleName());
        logger.write(Logger.Level.MAINSTEP, "Message:%n%s%n", message);
        final StringWriter errors = new StringWriter();
        ex.printStackTrace(new PrintWriter(errors));
        logger.write(Logger.Level.SUBSTEP, "Trace:%n%s%n", errors.toString());
    }

    static class SolverNameValidator implements IParameterValidator {
        @Override
        public void validate(String name, String value) throws ParameterException {
            if(!value.matches("[a-zA-Z0-9]+")) {
                throw new ParameterException(
                    String.format("Invalid solver name in parameter %s", name)
                );
            }
        }
    }

    static class SolverNameAndVersionValidator implements IParameterValidator {
        @Override
        public void validate(String name, String value) throws ParameterException {
            final var versionArr = value.split(":");

            if(versionArr.length != 2) {
                throw new ParameterException(String.format("Invalid version string %s in parameter %s", value, name));
            }

            if(!versionArr[0].matches("[a-zA-Z0-9]+") || !versionArr[1].matches("[a-zA-Z0-9]+")) {
                throw new ParameterException(
                    String.format("Invalid version string %s in parameter %s", value, name)
                );
            }
        }
    }
}
