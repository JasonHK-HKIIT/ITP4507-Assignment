class Musician
{
    private final String musicianID;
    private String mName;
    private int role;

    Musician(String mID)
    {
        musicianID = mID;
    }

    String getMID() { return musicianID; }

    int getRole() { return role; }

    void setRole(int role) { this.role = role; }

    String getName() { return mName; }

    void setName(String name) { mName = name; }

    static class Memento
    {
        private final Musician musician;
        private final int role;

        Memento(Musician musician)
        {
            this.musician = musician;
            role = musician.role;
        }

        void restore()
        {
            musician.role = role;
        }
    }
}
