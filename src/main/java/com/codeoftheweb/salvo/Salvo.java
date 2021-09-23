package com.codeoftheweb.salvo;

import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
public class Salvo {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
    @GenericGenerator(name = "native", strategy = "native")
    private Long id;

    //MtO Ship-->GamePlayer
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "gamePlayer_id")
    private  GamePlayer gamePlayerID;

    private int turnNumber;

    //Simple OtM Salvo-->SalvoLocations
    @ElementCollection
    @Column(name = "locations")
    private List<String> salvoLocations = new ArrayList<>();

    public Salvo() {
    }

    public Salvo(GamePlayer gamePlayerID, int turnNumber, List<String> salvoLocations) {
        this.gamePlayerID = gamePlayerID;
        this.turnNumber = turnNumber;
        this.salvoLocations = salvoLocations;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public GamePlayer getGamePlayerID() {
        return gamePlayerID;
    }

    public void setGamePlayerID(GamePlayer gamePlayerID) {
        this.gamePlayerID = gamePlayerID;
    }

    public int getTurnNumber() {
        return turnNumber;
    }

    public void setTurnNumber(int turnNumber) {
        this.turnNumber = turnNumber;
    }

    public List<String> getSalvoLocations() {
        return salvoLocations;
    }

    public void setSalvoLocations(List<String> salvoLocations) {
        this.salvoLocations = salvoLocations;
    }

}
