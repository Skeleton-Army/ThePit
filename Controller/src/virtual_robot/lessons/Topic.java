package virtual_robot.lessons;

import java.util.List;

public class Topic {
    public final String id;
    public final String title;
    public final String description;
    public final List<Lesson> lessons;

    public Topic(String id, String title, String description, List<Lesson> lessons) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.lessons = lessons;
    }
}
