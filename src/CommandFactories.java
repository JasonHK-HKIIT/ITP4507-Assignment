import java.util.Map;
import java.util.Stack;

/**
 * A collection of {@link Command} factory methods.
 */
@SuppressWarnings("ClassCanBeRecord")
class CommandFactories
{
    /** A mapping of {@link Ensemble} with its ID. */
    private final Map<String, Ensemble> ensembleMap;

    /** The undo stack. */
    private final Stack<Command> undoStack;
    /** The redo stack. */
    private final Stack<Command> redoStack;

    CommandFactories(Map<String, Ensemble> ensembleMap, Stack<Command> undoStack, Stack<Command> redoStack)
    {
        this.ensembleMap = ensembleMap;
        this.undoStack = undoStack;
        this.redoStack = redoStack;
    }

    CreateEnsembleCommand createCreateEnsembleCommand()
    {
        return new CreateEnsembleCommand(ensembleMap);
    }

    SetCurrentEnsembleCommand createSetCurrentEnsembleCommand()
    {
        return new SetCurrentEnsembleCommand(ensembleMap);
    }

    AddMusicianCommand createAddMusicianCommand(String activeEnsembleId)
    {
        return new AddMusicianCommand(ensembleMap, activeEnsembleId);
    }

    ModifyMusicianInstrumentCommand createModifyMusicianInstrumentCommand(String activeEnsembleId)
    {
        return new ModifyMusicianInstrumentCommand(ensembleMap, activeEnsembleId);
    }

    DeleteMusicianCommand createDeleteMusicianCommand(String activeEnsembleId)
    {
        return new DeleteMusicianCommand(ensembleMap, activeEnsembleId);
    }

    ShowEnsembleCommand createShowEnsembleCommand(String activeEnsembleId)
    {
        return new ShowEnsembleCommand(ensembleMap, activeEnsembleId);
    }

    DisplayAllEnsemblesCommand createDisplayAllEnsemblesCommand()
    {
        return new DisplayAllEnsemblesCommand(ensembleMap);
    }

    ChangeEnsembleNameCommand createChangeEnsembleNameCommand(String activeEnsembleId)
    {
        return new ChangeEnsembleNameCommand(ensembleMap, activeEnsembleId);
    }

    UndoCommand createUndoCommand()
    {
        return new UndoCommand(undoStack, redoStack);
    }

    RedoCommand createRedoCommand()
    {
        return new RedoCommand(undoStack, redoStack);
    }

    ListUndoRedoCommand createListUndoRedoCommand()
    {
        return new ListUndoRedoCommand(undoStack, redoStack);
    }

    ExitCommand createExitCommand()
    {
        return new ExitCommand();
    }
}
