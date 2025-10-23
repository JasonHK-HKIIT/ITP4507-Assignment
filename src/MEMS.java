import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Scanner;
import java.util.Stack;

public class MEMS
{
    /** A shared {@code stdin} scanner for the system. */
    public static final Scanner scanner = new Scanner(System.in);

    private static final Stack<Command> commandStack = new Stack<>();
    private static final Stack<Command> commandRedoStack = new Stack<>();

    /** A mapping of {@link Ensemble} with its ID. */
    private static final Map<String, Ensemble> ensembleMap = new HashMap<>();

    private static String activeEnsembleId = null;

    public static void setActiveEnsemble(String ensembleId)
    {
        if (!Objects.requireNonNull(ensembleId).equals(activeEnsembleId))
        {
            activeEnsembleId = ensembleId;
            System.out.printf("The current ensemble is changed to %s (ID: %s).%n",
                    ensembleMap.get(activeEnsembleId).getName(), activeEnsembleId);
        }
    }

    public static void setActiveEnsemble(Ensemble ensemble)
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

            if (!Objects.isNull(activeEnsembleId))
            {
                System.out.printf("The current ensemble is %s (ID: %s)%n", ensembleMap.get(activeEnsembleId).getName(), activeEnsembleId);
            }

            System.out.print("Enter command [c/s/a/m/d/se/sa/cn/u/r/l/x]: ");
            var code = scanner.nextLine().trim().toLowerCase();
            switch (code)
            {
                case "c" ->
                {
                    var command = createCreateEnsembleCommand();
                    if (command.execute()) { pushCommandStack(command); }
                }
                case "s" -> createSetCurrentEnsembleCommand().execute();
                case "a" ->
                {
                    if (Objects.isNull(activeEnsembleId))
                    {
                        System.err.println("No ensemble to add to.");
                    }
                    else
                    {
                        var command = createAddMusicianCommand();
                        if (command.execute()) { pushCommandStack(command); }
                    }
                }
                case "m" ->
                {
                    if (Objects.isNull(activeEnsembleId))
                    {
                        System.err.println("No ensemble to edit from.");
                    }
                    else
                    {
                        var command = createModifyMusicianInstrumentCommand();
                        if (command.execute()) { pushCommandStack(command); }
                    }
                }
                case "d" ->
                {
                    if (Objects.isNull(activeEnsembleId))
                    {
                        System.err.println("No ensemble to delete from.");
                    }
                    else
                    {
                        var command = createDeleteMusicianCommand();
                        if (command.execute()) { pushCommandStack(command); }
                    }
                }
                case "se" ->
                {
                    if (Objects.isNull(activeEnsembleId))
                    {
                        System.err.println("Nothing to show.");
                    }
                    else
                    {
                        ensembleMap.get(activeEnsembleId).showEnsemble();
                    }
                }
                case "sa" -> createDisplayAllEnsemblesCommand().execute();
                case "cn" ->
                {
                    if (Objects.isNull(activeEnsembleId))
                    {
                        System.err.println("No ensemble to rename.");
                    }
                    else
                    {
                        var command = createChangeEnsembleNameCommand();
                        if (command.execute()) { pushCommandStack(command); }
                    }
                }
                case "u" -> createUndoCommand().execute();
                case "r" -> createRedoCommand().execute();
                case "l" -> createListUndoRedoCommand().execute();
                case "x" -> createExitCommand().execute();
                default -> System.err.println("Invalid command!");
            }

            System.out.println();
            System.out.println();
        }
    }

    /**
     * Push a command to the stack. Then clear the undo stack if it's not empty.
     */
    private static void pushCommandStack(Command command)
    {
        if (!commandRedoStack.isEmpty()) { commandRedoStack.clear(); }
        commandStack.push(command);
    }

    private static CreateEnsembleCommand createCreateEnsembleCommand()
    {
        return new CreateEnsembleCommand(ensembleMap);
    }

    private static SetCurrentEnsembleCommand createSetCurrentEnsembleCommand()
    {
        return new SetCurrentEnsembleCommand(ensembleMap);
    }

    private static AddMusicianCommand createAddMusicianCommand()
    {
        return new AddMusicianCommand(ensembleMap.get(activeEnsembleId));
    }

    private static ModifyMusicianInstrumentCommand createModifyMusicianInstrumentCommand()
    {
        return new ModifyMusicianInstrumentCommand(ensembleMap.get(activeEnsembleId));
    }

    private static DeleteMusicianCommand createDeleteMusicianCommand()
    {
        return new DeleteMusicianCommand(ensembleMap.get(activeEnsembleId));
    }

    private static DisplayAllEnsemblesCommand createDisplayAllEnsemblesCommand()
    {
        return new DisplayAllEnsemblesCommand(ensembleMap);
    }

    private static ChangeEnsembleNameCommand createChangeEnsembleNameCommand()
    {
        return new ChangeEnsembleNameCommand(ensembleMap.get(activeEnsembleId));
    }

    private static UndoCommand createUndoCommand()
    {
        return new UndoCommand(commandStack, commandRedoStack);
    }

    private static RedoCommand createRedoCommand()
    {
        return new RedoCommand(commandStack, commandRedoStack);
    }

    private static ListUndoRedoCommand createListUndoRedoCommand()
    {
        return new ListUndoRedoCommand(commandStack, commandRedoStack);
    }

    private static ExitCommand createExitCommand()
    {
        return new ExitCommand();
    }

    public static class Memento
    {
        private final String activeEnsembleId;

        public Memento()
        {
            activeEnsembleId = MEMS.activeEnsembleId;
        }

        public void restore()
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