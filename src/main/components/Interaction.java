package main.components;

public abstract class Interaction extends Component{

    @Override
    public void update(float dt){
        selfUpdate(dt);
    }
    @Override
    public void start(){
        selfStart();
    }

    public abstract void selfUpdate(float dt);
    public abstract void selfStart();
}
