package main.components;

import java.util.ArrayList;
import java.util.List;

public class InteractionManager extends Component{
    public List<Interaction> interactions = new ArrayList<>();
    public InteractionManager(){
        this.interactions = interactions;
    }

    public void add(Interaction inter){
        interactions.add(inter);
    }
}
