public class Musician
{
    private final String musicianID;
    private String mName;
    private int role;

    public Musician(String mID)
    {
        musicianID = mID;
    }

    public String getMID() { return musicianID; }

    public int getRole() { return role; }

    public void setRole(int role) { this.role = role; }

    public String getName() { return mName; }

    public void setName(String name) { mName = name; }

    public static class Memento
    {
        private final Musician musician;
        private final int role;

        public Memento(Musician musician)
        {
            this.musician = musician;
            role = musician.role;
        }

        public void restore()
        {
            musician.role = role;
        }
    }
}
