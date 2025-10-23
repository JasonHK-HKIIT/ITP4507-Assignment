import java.util.AbstractList;
import java.util.ArrayList;

@SuppressWarnings("FieldMayBeFinal")
public abstract class Ensemble
{
    private String ensembleID;
    private String eName;
    private AbstractList<Musician> musicians;

    public Ensemble(String eId)
    {
        ensembleID = eId;
        musicians = new ArrayList<>();
    }

    public String getEnsembleID() { return ensembleID; }

    public String getName() { return eName; }

    public void setName(String name) { eName = name; }

    public void addMusician(Musician m) { musicians.add(m); }

    public void dropMusician(Musician m) { musicians.remove(m); }

    public Iterable<Musician> getMusicians() { return musicians; }

    public abstract void updateMusicianRole();

    public abstract void showEnsemble();
}

class OrchestraEnsemble extends Ensemble
{
    private static final int VIOLINIST_ROLE = 1;
    private static final int CELLIST_ROLE = 2;

    public OrchestraEnsemble(String eId)
    {
        super(eId);
    }

    @Override
    public void updateMusicianRole()
    {
        System.out.print("Musician role [1 = violinist / 2 = cellist]: ");
    }

    @Override
    public void showEnsemble()
    {
        var violinists = new ArrayList<Musician>();
        var cellists = new ArrayList<Musician>();
        for (var musician : getMusicians())
        {
            switch (musician.getRole())
            {
                case VIOLINIST_ROLE -> violinists.add(musician);
                case CELLIST_ROLE -> cellists.add(musician);
                default -> throw new IllegalStateException(String.format("Invalid role ID: %d", musician.getRole()));
            }
        }

        System.out.printf("Orchestra Ensemble %s (%s)%n", getName(), getEnsembleID());
        System.out.println("Violinist(s):");
        if (violinists.isEmpty())
        {
            System.out.println("  EMPTY");
        }
        else
        {
            for (var violinist : violinists)
            {
                System.out.printf("- %s (ID: %s)%n", violinist.getName(), violinist.getMID());
            }
        }
        System.out.println("Cellist(s):");
        if (cellists.isEmpty())
        {
            System.out.println("  EMPTY");
        }
        else
        {
            for (var cellist : cellists)
            {
                System.out.printf("- %s (ID: %s)%n", cellist.getName(), cellist.getMID());
            }
        }
    }
}

class JazzBandEnsemble extends Ensemble
{
    private static final int PIANIST_ROLE = 1;
    private static final int SAXOPHONIST_ROLE = 2;
    private static final int DRUMMER_ROLE = 3;

    public JazzBandEnsemble(String eId)
    {
        super(eId);
    }

    @Override
    public void updateMusicianRole()
    {
        System.out.print("Musician role [1 = pianist / 2 = saxophonist / 3 = drummer]: ");
    }

    @Override
    public void showEnsemble()
    {
        var pianists = new ArrayList<Musician>();
        var saxophonists = new ArrayList<Musician>();
        var drummers = new ArrayList<Musician>();
        for (var musician : getMusicians())
        {
            switch (musician.getRole())
            {
                case PIANIST_ROLE -> pianists.add(musician);
                case SAXOPHONIST_ROLE -> saxophonists.add(musician);
                case DRUMMER_ROLE -> drummers.add(musician);
                default -> throw new IllegalStateException(String.format("Invalid role ID: %d", musician.getRole()));
            }
        }

        System.out.printf("Jazz Band Ensemble %s (%s)%n", getName(), getEnsembleID());
        System.out.println("Pianist(s):");
        if (pianists.isEmpty())
        {
            System.out.println("  EMPTY");
        }
        else
        {
            for (var pianist : pianists)
            {
                System.out.printf("- %s (ID: %s)%n", pianist.getName(), pianist.getMID());
            }
        }
        System.out.println("Saxophonist(s):");
        if (saxophonists.isEmpty())
        {
            System.out.println("  EMPTY");
        }
        else
        {
            for (var saxophonist : saxophonists)
            {
                System.out.printf("- %s (ID: %s)%n", saxophonist.getName(), saxophonist.getMID());
            }
        }
        System.out.println("Drummer(s):");
        if (drummers.isEmpty())
        {
            System.out.println("  EMPTY");
        }
        else
        {
            for (var drummer : drummers)
            {
                System.out.printf("- %s (ID: %s)%n", drummer.getName(), drummer.getMID());
            }
        }
    }
}
