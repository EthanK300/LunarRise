package main.util;

import main.player.PlayerController;

import javax.swing.*;
import java.awt.*;

public class telemetry{
    String playerVelocity1 = "playerVelocity: ";
    String playerPosition1 = "playerPosition: ";
    String playerAcceleration1 = "playerAcceleration: ";
    String backDropPosition1 = "backDropPosition: ";
    String cameraPosition1 = "cameraPosition: ";
    String jumpTime1 = "Jump Time: ";
    String highestYPos1 = "Highest Y Pos: ";
    JFrame tele = new JFrame("Telemetry Data");
    JLabel playerVelocity = new JLabel(playerVelocity1);
    JLabel playerPosition = new JLabel(playerPosition1);
    JLabel playerAcceleration = new JLabel(playerAcceleration1);
    JLabel backDropPosition = new JLabel(backDropPosition1);
    JLabel cameraPosition = new JLabel(cameraPosition1);
    JLabel jumpTime = new JLabel(jumpTime1);
    JLabel highestYPos = new JLabel(highestYPos1);

    public telemetry(){
        tele.setSize(400,400);
        tele.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        tele.setAlwaysOnTop(true);
        tele.setLayout(new BoxLayout(tele.getContentPane(), BoxLayout.Y_AXIS));
        tele.add(playerVelocity);
        tele.add(playerPosition);
        tele.add(playerAcceleration);
        tele.add(backDropPosition);
        tele.add(cameraPosition);
        tele.add(jumpTime);
        tele.add(highestYPos);
        tele.setVisible(true);
    }
    public void update(){
        try {
            playerVelocity1 = "Velocity: " + PlayerController.getVelocityStatic().x + ", " + PlayerController.getVelocityStatic().y;
            playerAcceleration1 = "Acceleration: " + PlayerController.getAccelerationStatic().x + ", " + PlayerController.getAccelerationStatic().y;
            playerPosition1 = "Position" + PlayerController.getPosition().x + ", " + PlayerController.getPosition().y;
            jumpTime1 = "Jump Time: " + PlayerController.getJumpTime();
            highestYPos1 = "Highest Y Pos: " + PlayerController.getHighestYPos();

            playerVelocity.setText(playerVelocity1);
            playerPosition.setText(playerPosition1);
            playerAcceleration.setText(playerAcceleration1);
            backDropPosition.setText(backDropPosition1);
            cameraPosition.setText(cameraPosition1);
            jumpTime.setText(jumpTime1);
            highestYPos.setText(highestYPos1);

            tele.repaint();
        }catch(NullPointerException e){
            //do nothing (assuming the game hasn't started, ofc there are no values)
        }
    }
    public void hideTele(){
        tele.setVisible(false);
    }
}