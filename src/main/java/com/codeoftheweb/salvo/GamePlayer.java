package com.codeoftheweb.salvo;

import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.time.Instant;
import java.util.*;

@Entity
public class GamePlayer {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
    @GenericGenerator(name = "native", strategy = "native")
    private Long id;
    private Date joinDate;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "player_id")
    private Player playerID;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "game_id")
    private  Game gameID;

    //OtM GamePlayers --> Ship
    @OneToMany(mappedBy= "gamePlayerID", fetch=FetchType.EAGER)
    Set<Ship> ships = new HashSet<>();

    //OtM GamePlayers --> Salvo
    @OneToMany(mappedBy = "gamePlayerID", fetch=FetchType.EAGER)
    Set<Salvo> salvos = new HashSet<>();


    public GamePlayer() {}

    public GamePlayer(Date joinDate, Player playerID, Game gameID) {
        this.joinDate = joinDate;
        this.playerID = playerID;
        this.gameID = gameID;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Date getJoinDate() {
        return joinDate;
    }

    public void setJoinDate(Date joinDate) {
        this.joinDate = joinDate;
    }

    public Player getPlayerID() {
        return playerID;
    }

    public void setPlayerID(Player playerID) {
        this.playerID = playerID;
    }

    public Game getGameID() {
        return gameID;
    }

    public void setGameID(Game gameID) {
        this.gameID = gameID;
    }

    public Set<Ship> getShips() {
        return ships;
    }

    public void setShips(Set<Ship> ships) {
        this.ships = ships;
    }

    public Set<Salvo> getSalvos() {
        return salvos;
    }

    public void setSalvos(Set<Salvo> salvos) {
        this.salvos = salvos;
    }

    //Metodo getScore
    //Para este player en particular le paso "this.gameID" para obtener este juego en particular
    public Score getScore(){
      return playerID.getScore(this.gameID);
  }

    /*
    public void addShips(List<Ship> ships){ //metodo para crear y agregar barcos a mi lista de GamePlayers
        ships.forEach(ship -> {ship.setGamePlayerID(this); //a este ship le asigno "este (this)" game player
        this.ships.add(ship);}); // en mi lista de ships de este gameplayer guardo el ship a este mismo gameplayer
    }
    //ships.add(ship); // en mi lista de ships de este gameplayer guardo el ship a este mismo gameplayer
     */


    public void addShip(Ship ship) {
        ship.setGamePlayerID(this);//a este ship le asigno "este (this)" game player
    }

    public void addSalvos(Salvo salvo) {
        salvo.setGamePlayerID(this);//a este ship le asigno "este (this)" game player
    }


}


