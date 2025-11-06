import java.util.Map;
import java.util.Objects;
import java.util.Stack;

public interface Command
{
    /**
     * Execute the command.
     *
     * @return Whether the executed command should be pushed into an undo stack.
     */
    boolean execute();

    /**
     * Undo the command.
     */
    void undo();

    /**
     * Redo the command.
     */
    void redo();
}

class CreateEnsembleCommand implements Command
{
    private final MEMS.State state = new MEMS.State();
    private final Map<String, Ensemble> ensembleMap;
    private Ensemble ensemble;

    public CreateEnsembleCommand(Map<String, Ensemble> ensembleMap)
    {
        this.ensembleMap = ensembleMap;
    }

    @Override
    public boolean execute()
    {
        System.out.print("Ensemble type [o = orchestra / j = jazz band]: ");
        var type = MEMS.scanner.nextLine().trim().toLowerCase();
        if (type.isEmpty())
        {
            System.err.println("Ensemble type cannot be empty!");
            return false;
        }

        System.out.print("Ensemble ID: ");
        var ensembleId = MEMS.scanner.nextLine().trim();
        if (ensembleId.isEmpty())
        {
            System.err.println("Ensemble ID cannot be empty!");
            return false;
        }
        else if (ensembleMap.containsKey(ensembleId))
        {
            System.err.println("Ensemble ID already exist!");
            return false;
        }

        ensemble = switch (type)
        {
            case "o" -> createOrchestraEnsemble(ensembleId);
            case "j" -> createJazzBandEnsemble(ensembleId);
            default -> null;
        };
        if (Objects.isNull(ensemble))
        {
            System.err.println("Invalid ensemble type!");
            return false;
        }

        System.out.print("Ensemble name: ");
        var ensembleName = MEMS.scanner.nextLine().trim();
        if (ensembleName.isEmpty())
        {
            System.err.println("Ensemble name cannot be empty!");
            return false;
        }

        ensemble.setName(ensembleName);
        ensembleMap.put(ensemble.getEnsembleID(), ensemble);
        System.out.println("Ensemble is created.");
        MEMS.setActiveEnsemble(ensemble);
        return true;
    }

    @Override
    public void undo()
    {
        ensembleMap.remove(ensemble.getEnsembleID());
        state.restore();
    }

    @Override
    public void redo()
    {
        ensembleMap.put(ensemble.getEnsembleID(), ensemble);
        MEMS.setActiveEnsemble(ensemble);
    }

    @Override
    public String toString()
    {
        return String.format("Create %s: %s (ID: %s)", ensemble.getClass().getSimpleName(), ensemble.getName(), ensemble.getEnsembleID());
    }

    private OrchestraEnsemble createOrchestraEnsemble(String ensembleId)
    {
        return new OrchestraEnsemble(ensembleId);
    }

    private JazzBandEnsemble createJazzBandEnsemble(String ensembleId)
    {
        return new JazzBandEnsemble(ensembleId);
    }
}

@SuppressWarnings("ClassCanBeRecord")
class SetCurrentEnsembleCommand implements Command
{
    private final Map<String, Ensemble> ensembleMap;

    public SetCurrentEnsembleCommand(Map<String, Ensemble> ensembleMap)
    {
        this.ensembleMap = ensembleMap;
    }

    @Override
    public boolean execute()
    {
        System.out.print("Ensemble ID: ");
        String activeEnsembleId = MEMS.scanner.nextLine().trim();
        if (activeEnsembleId.isEmpty())
        {
            System.err.println("Ensemble ID cannot be empty!");
            return false;
        }
        else if (!ensembleMap.containsKey(activeEnsembleId))
        {
            System.err.println("Ensemble ID does not exist!");
            return false;
        }

        MEMS.setActiveEnsemble(activeEnsembleId);
        return false;
    }

    @Override
    public void undo() {}

    @Override
    public void redo() {}
}

class AddMusicianCommand implements Command
{
    private final MEMS.State state = new MEMS.State();
    private final Ensemble ensemble;
    private Musician musician;

    public AddMusicianCommand(Map<String, Ensemble> ensembleMap, String activeEnsembleId)
    {
        ensemble = Objects.nonNull(activeEnsembleId) ? ensembleMap.get(activeEnsembleId) : null;
    }

    @Override
    public boolean execute()
    {
        if (Objects.isNull(ensemble))
        {
            System.err.println("No ensemble to add to.");
            return false;
        }

        System.out.print("Musician info (ID, name): ");
        var inputs = MEMS.scanner.nextLine().split(",", 2);
        if (inputs.length != 2)
        {
            System.err.println("Malformed input! Must be ID and name separated by comma, e.g. \"M001, Bob Dylan\".");
            return false;
        }

        var musicianId = inputs[0].trim();
        if (musicianId.isEmpty())
        {
            System.err.println("Musician ID cannot be empty!");
            return false;
        }
        for (var musician : ensemble.getMusicians())
        {
            if (Objects.equals(musician.getMID(), musicianId))
            {
                System.err.println("Musician ID already exist!");
                return false;
            }
        }

        var musicianName = inputs[1].trim();
        if (musicianName.isEmpty())
        {
            System.err.println("Musician name cannot be empty!");
            return false;
        }

        musician = createMusician(musicianId, musicianName);

        try
        {
            ensemble.updateMusicianRole();
            var musicianRole = Integer.parseInt(MEMS.scanner.nextLine());
            musician.setRole(musicianRole);
        }
        catch (NumberFormatException ex)
        {
            System.err.println("Invalid musician role!");
            return false;
        }

        ensemble.addMusician(musician);
        System.out.println("Musician is created.");
        return true;
    }

    @Override
    public void undo()
    {
        ensemble.dropMusician(musician);
        state.restore();
    }

    @Override
    public void redo()
    {
        ensemble.addMusician(musician);
        MEMS.setActiveEnsemble(ensemble);
    }

    @Override
    public String toString()
    {
        return String.format("Add musician: %s (ID: %s), role %d", musician.getName(), musician.getMID(), musician.getRole());
    }

    private Musician createMusician(String musicianId, String musicianName)
    {
        var musician = new Musician(musicianId);
        musician.setName(musicianName);

        return musician;
    }
}

class ModifyMusicianInstrumentCommand implements Command
{
    private final MEMS.State state = new MEMS.State();
    private final Ensemble ensemble;
    private Musician musician;
    private Musician.Memento memento;
    private int musicianRole;

    public ModifyMusicianInstrumentCommand(Map<String, Ensemble> ensembleMap, String activeEnsembleId)
    {
        ensemble = Objects.nonNull(activeEnsembleId) ? ensembleMap.get(activeEnsembleId) : null;
    }

    @Override
    public boolean execute()
    {
        if (Objects.isNull(ensemble))
        {
            System.err.println("No ensemble to edit from.");
            return false;
        }

        System.out.print("Musician ID: ");
        var musicianId = MEMS.scanner.nextLine().trim();
        if (musicianId.isEmpty())
        {
            System.err.println("Musician ID cannot be empty!");
            return false;
        }
        for (var musician : ensemble.getMusicians())
        {
            if (Objects.equals(musician.getMID(), musicianId))
            {
                this.musician = musician;
                memento = new Musician.Memento(musician);
                break;
            }
        }
        if (Objects.isNull(musician))
        {
            System.err.println("Musician ID does not exist!");
            return false;
        }

        try
        {
            ensemble.updateMusicianRole();
            musicianRole = Integer.parseInt(MEMS.scanner.nextLine());
            musician.setRole(musicianRole);
        }
        catch (NumberFormatException ex)
        {
            System.err.println("Invalid musician role!");
            return false;
        }

        System.out.println("Musician role is updated.");
        return true;
    }

    @Override
    public void undo()
    {
        memento.restore();
        state.restore();
    }

    @Override
    public void redo()
    {
        musician.setRole(musicianRole);
        MEMS.setActiveEnsemble(ensemble);
    }

    @Override
    public String toString()
    {
        return String.format("Modify musician role: %s (ID: %s), role %d", musician.getName(), musician.getMID(), musician.getRole());
    }
}

class DeleteMusicianCommand implements Command
{
    private final MEMS.State state = new MEMS.State();
    private final Ensemble ensemble;
    private Musician musician;

    public DeleteMusicianCommand(Map<String, Ensemble> ensembleMap, String activeEnsembleId)
    {
        ensemble = Objects.nonNull(activeEnsembleId) ? ensembleMap.get(activeEnsembleId) : null;
    }

    @Override
    public boolean execute()
    {
        if (Objects.isNull(ensemble))
        {
            System.err.println("No ensemble to delete from.");
            return false;
        }

        System.out.print("Musician ID: ");
        var musicianId = MEMS.scanner.nextLine().trim();
        if (musicianId.isEmpty())
        {
            System.err.println("Musician ID cannot be empty!");
            return false;
        }
        for (var musician : ensemble.getMusicians())
        {
            if (Objects.equals(musician.getMID(), musicianId))
            {
                this.musician = musician;
                break;
            }
        }
        if (Objects.isNull(musician))
        {
            System.err.println("Musician ID does not exist!");
            return false;
        }

        ensemble.dropMusician(musician);
        System.out.println("Musician is deleted.");
        return true;
    }

    @Override
    public void undo()
    {
        ensemble.addMusician(musician);
        state.restore();
    }

    @Override
    public void redo()
    {
        ensemble.dropMusician(musician);
        MEMS.setActiveEnsemble(ensemble);
    }

    @Override
    public String toString()
    {
        return String.format("Delete musician: %s (ID: %s)", musician.getName(), musician.getMID());
    }
}

class ShowEnsembleCommand implements Command
{
    private final Ensemble ensemble;

    public ShowEnsembleCommand(Map<String, Ensemble> ensembleMap, String activeEnsembleId)
    {
        ensemble = Objects.nonNull(activeEnsembleId) ? ensembleMap.get(activeEnsembleId) : null;
    }

    @Override
    public boolean execute()
    {
        if (Objects.isNull(ensemble))
        {
            System.err.println("Nothing to show.");
            return false;
        }

        ensemble.showEnsemble();
        return false;
    }

    @Override
    public void undo() {}

    @Override
    public void redo() {}
}

@SuppressWarnings("ClassCanBeRecord")
class DisplayAllEnsemblesCommand implements Command
{
    private final Map<String, Ensemble> ensembleMap;

    public DisplayAllEnsemblesCommand(Map<String, Ensemble> ensembleMap)
    {
        this.ensembleMap = ensembleMap;
    }

    @Override
    public boolean execute()
    {
        if (ensembleMap.isEmpty())
        {
            System.err.println("No ensembles to display!");
            return false;
        }

        for (var ensemble : ensembleMap.values())
        {
            System.out.printf("- %s: %s (ID: %s)%n", ensemble.getClass().getSimpleName(), ensemble.getName(), ensemble.getEnsembleID());
        }

        return false;
    }

    @Override
    public void undo() {}

    @Override
    public void redo() {}
}

class ChangeEnsembleNameCommand implements Command
{
    private final MEMS.State state = new MEMS.State();
    private final Ensemble ensemble;
    private final Ensemble.Memento memento;
    private String ensembleName;

    public ChangeEnsembleNameCommand(Map<String, Ensemble> ensembleMap, String activeEnsembleId)
    {
        ensemble = Objects.nonNull(activeEnsembleId) ? ensembleMap.get(activeEnsembleId) : null;
        memento = new Ensemble.Memento(ensemble);
    }

    @Override
    public boolean execute()
    {
        if (Objects.isNull(ensemble))
        {
            System.err.println("No ensemble to rename.");
            return false;
        }

        System.out.print("New ensemble name: ");
        ensembleName = MEMS.scanner.nextLine().trim();
        if (ensembleName.isEmpty())
        {
            System.err.println("Ensemble name cannot be empty!");
            return false;
        }

        ensemble.setName(ensembleName);
        System.out.println("Ensemble name is updated.");
        return true;
    }

    @Override
    public void undo()
    {
        memento.restore();
        state.restore();
    }

    @Override
    public void redo()
    {
        ensemble.setName(ensembleName);
        MEMS.setActiveEnsemble(ensemble);
    }

    @Override
    public String toString()
    {
        return String.format("Change ensemble name: %s (ID: %s)", ensembleName, ensemble.getEnsembleID());
    }
}

@SuppressWarnings("ClassCanBeRecord")
class UndoCommand implements Command
{
    private final Stack<Command> commandStack;
    private final Stack<Command> commandRedoStack;

    public UndoCommand(Stack<Command> commandStack, Stack<Command> commandRedoStack)
    {
        this.commandStack = commandStack;
        this.commandRedoStack = commandRedoStack;
    }

    @Override
    public boolean execute()
    {
        if (commandStack.isEmpty())
        {
            System.err.println("Nothing to undo.");
            return false;
        }

        var command = commandStack.pop();
        System.out.printf("Command is undone: %s%n", command);
        command.undo();
        commandRedoStack.push(command);
        return false;
    }

    @Override
    public void undo() {}

    @Override
    public void redo() {}
}

@SuppressWarnings("ClassCanBeRecord")
class RedoCommand implements Command
{
    private final Stack<Command> commandStack;
    private final Stack<Command> commandRedoStack;

    public RedoCommand(Stack<Command> commandStack, Stack<Command> commandRedoStack)
    {
        this.commandStack = commandStack;
        this.commandRedoStack = commandRedoStack;
    }

    @Override
    public boolean execute()
    {
        if (commandRedoStack.isEmpty())
        {
            System.err.println("Nothing to redo.");
            return false;
        }

        var command = commandRedoStack.pop();
        System.out.printf("Command is redone: %s%n", command);
        command.redo();
        commandStack.push(command);
        return false;
    }

    @Override
    public void undo() {}

    @Override
    public void redo() {}
}

@SuppressWarnings("ClassCanBeRecord")
class ListUndoRedoCommand implements Command
{
    private final Stack<Command> commandStack;
    private final Stack<Command> commandRedoStack;

    public ListUndoRedoCommand(Stack<Command> commandStack, Stack<Command> commandRedoStack)
    {
        this.commandStack = commandStack;
        this.commandRedoStack = commandRedoStack;
    }

    @Override
    public boolean execute()
    {
        System.out.println();
        System.out.println("Undo List");
        if (commandStack.isEmpty())
        {
            System.out.println("  EMPTY");
        }
        else
        {
            for (var command : commandStack)
            {
                if (command instanceof SetCurrentEnsembleCommand) { continue; }
                System.out.printf("- %s%n", command);
            }
        }

        System.out.println();
        System.out.println("Redo List");
        if (commandRedoStack.isEmpty())
        {
            System.out.println("  EMPTY");
        }
        else
        {
            for (var command : commandRedoStack)
            {
                if (command instanceof SetCurrentEnsembleCommand) { continue; }
                System.out.printf("- %s%n", command);
            }
        }

        return false;
    }

    @Override
    public void undo() {}

    @Override
    public void redo() {}
}

/**
 * Command to exit the program.
 */
class ExitCommand implements Command
{
    @Override
    public boolean execute()
    {
        System.exit(0);
        return false;
    }

    @Override
    public void undo() {}

    @Override
    public void redo() {}
}
