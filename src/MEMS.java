import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Scanner;
import java.util.Stack;

public class MEMS
{
    /** A shared {@code stdin} scanner for the system. */
    static final Scanner scanner = new Scanner(System.in);

    /** A mapping of {@link Ensemble} with its ID. */
    private static final Map<String, Ensemble> ensembleMap = new HashMap<>();

    private static final Stack<Command> undoStack = new Stack<>();
    private static final Stack<Command> redoStack = new Stack<>();

    private static final CommandFactories commandFactories = new CommandFactories(ensembleMap, undoStack, redoStack);

    /** The ID of the currently active {@link Ensemble}. {@code null} if no ensemble has been created yet. */
    private static String activeEnsembleId = null;

    /**
     * Updates the active ensemble on which commands perform operations.
     *
     * @param ensembleId The ID of an ensemble.
     */
    static void setActiveEnsemble(String ensembleId)
    {
        if (!Objects.requireNonNull(ensembleId).equals(activeEnsembleId))
        {
            activeEnsembleId = ensembleId;
            System.out.printf("The current ensemble is changed to %s (ID: %s).%n",
                    ensembleMap.get(activeEnsembleId).getName(), activeEnsembleId);
        }
    }

    /**
     * Updates the active ensemble on which commands perform operations.
     *
     * @param ensemble The ensemble.
     */
    static void setActiveEnsemble(Ensemble ensemble)
    {
        setActiveEnsemble(ensemble.getEnsembleID());
    }

    @SuppressWarnings("InfiniteLoopStatement")
    public static void main(String[] args)
    {
        while (true)
        {
            System.out.println("Music Ensembles Management System (MEMS)");
            System.out.println("c = create ensemble, s = set current ensemble, a = add musician, m = modify musician's instrument,\nd = delete musician, se = show ensemble, sa = display all ensembles, cn = change ensemble's name,\nu = undo, r = redo, l = list undo/redo, x = exit system");

            if (Objects.nonNull(activeEnsembleId))
            {
                System.out.printf("The current ensemble is %s (ID: %s)%n", ensembleMap.get(activeEnsembleId).getName(), activeEnsembleId);
            }

            System.out.print("Enter command [c/s/a/m/d/se/sa/cn/u/r/l/x]: ");
            var command = switch (scanner.nextLine().trim().toLowerCase())
            {
                case "c" -> commandFactories.createCreateEnsembleCommand();
                case "s" -> commandFactories.createSetCurrentEnsembleCommand();
                case "a" -> commandFactories.createAddMusicianCommand(activeEnsembleId);
                case "m" -> commandFactories.createModifyMusicianInstrumentCommand(activeEnsembleId);
                case "d" -> commandFactories.createDeleteMusicianCommand(activeEnsembleId);
                case "se" -> commandFactories.createShowEnsembleCommand(activeEnsembleId);
                case "sa" -> commandFactories.createDisplayAllEnsemblesCommand();
                case "cn" -> commandFactories.createChangeEnsembleNameCommand(activeEnsembleId);
                case "u" -> commandFactories.createUndoCommand();
                case "r" -> commandFactories.createRedoCommand();
                case "l" -> commandFactories.createListUndoRedoCommand();
                case "x" -> commandFactories.createExitCommand();
                default ->
                {
                    System.err.println("Invalid command!");
                    yield null;
                }
            };

            if (Objects.nonNull(command) && command.execute())
            {
                if (!redoStack.isEmpty()) { redoStack.clear(); }
                undoStack.push(command);
            }

            System.out.println();
            System.out.println();
        }
    }

    static class State
    {
        private final String activeEnsembleId;

        State()
        {
            activeEnsembleId = MEMS.activeEnsembleId;
        }

        void restore()
        {
            if (!Objects.equals(MEMS.activeEnsembleId, activeEnsembleId))
            {
                MEMS.activeEnsembleId = activeEnsembleId;
                if (Objects.isNull(activeEnsembleId))
                {
                    System.out.println("The current ensemble is changed to NONE.");
                    return;
                }

                System.out.printf("The current ensemble is changed to %s (ID: %s).%n",
                        ensembleMap.get(activeEnsembleId).getName(), activeEnsembleId);
            }
        }
    }
}