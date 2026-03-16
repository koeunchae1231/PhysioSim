package physiosim.event;

public final class Command {
    private final CommandId id;
    private final CommandDirection dir;
    private final int level;

    public Command(CommandId id, CommandDirection dir, int level) {
        this.id = id;
        this.dir = dir;
        this.level = level;
    }

    public CommandId getId() { return id; }
    public CommandDirection getDir() { return dir; }
    public int getLevel() { return level; }
}
