package com.codeoftheweb.salvo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class SalvoController {

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private GameRepository gameRepository;

    @Autowired
    private GamePlayerRepository gamePlayerRepository;

    @Autowired
    private ShipRepository shipRepository;

    @Autowired
    private SalvoRepository salvoRepository;

    @Autowired
    private PlayerRepository playerRepository;

    @Autowired
    private ScoreRepository scoreRepository;


    @RequestMapping("/games")
    public Map<String, Object> getGames(Authentication authentication) {
        Map<String, Object> dto = new LinkedHashMap<String, Object>();
        if (!isGuest(authentication)) {
            dto.put("player", makePlayersDTO(playerRepository.findByUserName(authentication.getName())));
        } else {
            dto.put("player", "Guest");
        }

        dto.put("games", gameRepository.findAll()
                .stream()
                .map(game1 -> makeGamesDTO(game1))
                .collect(Collectors.toList()));
        return dto;
    }

    @RequestMapping("/game_view/{nn}")
    public ResponseEntity<Map<String, Object>> findGame(@PathVariable Long nn, Authentication authentication) {

        if (isGuest(authentication)) {
            return new ResponseEntity<>(makeMap("error", "UNAUTHORIZED: player no login"), HttpStatus.FORBIDDEN);
        }
        //Tengo q validar si el id del player del gameplayer q me pasan por parametro es igual al id del player logueado.
        //Si es distinto, rechazo la peticion, sino muestro el DTO de GamesView que ya tenia.

        Optional<GamePlayer> gp = gamePlayerRepository.findById(nn);
        Player player = playerRepository.findByUserName(authentication.getName());

        //if(gp.get().getPlayerID().getId() != player.getId()){
        if (preventCheat(gp.get(), player)) {
            return new ResponseEntity<>(makeMap("error", "player is UNAUTHORIZED"), HttpStatus.FORBIDDEN);
        } else {
            if(gp.get().getGameID().getGamePlayers().size() == 2){
                setScores(gp.get(),gameState(gp.get()));
            }
            return new ResponseEntity<>(makeGamesViewDTO(gp.get()), HttpStatus.OK);
        }
    }


    //DTOS
    private Map<String, Object> makeGamesDTO(Game game) {
        Map<String, Object> dto = new LinkedHashMap<String, Object>();
        dto.put("id", game.getId());
        dto.put("created", game.getCreationDate());
        dto.put("gamePlayers", game.getGamePlayers()
                .stream()
                .map(gamePlayer -> makeGamePlayersDTO(gamePlayer))
                .collect(Collectors.toList()));
        //dto.put("Players", game.getGamePlayers()
        //        .stream()
        //        .map(gamePlayer -> makeGamePlayersDTO(gamePlayer))
        //        .collect(Collectors.toList()));
        dto.put("scores", game.getScore().stream().map(score -> makeScoreDTO(score)).collect(Collectors.toList()));
        return dto;
    }

    private Map<String, Object> makeGamePlayersDTO(GamePlayer gameplayer) {
        Map<String, Object> dto = new LinkedHashMap<String, Object>();
        dto.put("id", gameplayer.getId());
        dto.put("player", makePlayersDTO(gameplayer.getPlayerID()));
        //Metodo getScore
        //Uso el metodo para validar que haya score para el gameplayer
        //Uso el metodo para obtener del player de este gameplayer el score
        //    if(gameplayer.getScore() != null){
        //        dto.put("score", gameplayer.getScore().getScore());
        //dto.put("finishDate", gameplayer.getScore().getFinishDate());
        //        }
        return dto;
    }

    private Map<String, Object> makePlayersDTO(Player player) {
        Map<String, Object> dto = new LinkedHashMap<String, Object>();
        dto.put("id", player.getId());
        dto.put("email", player.getUserName());
        return dto;
    }

    private Map<String, Object> makeGamesViewDTO(GamePlayer gamePlayer) {
        Map<String, Object> dto = new LinkedHashMap<String, Object>();
        dto.put("id", gamePlayer.getGameID().getId());
        dto.put("created", gamePlayer.getGameID().getCreationDate());
        dto.put("gameState", gameState(gamePlayer));
        dto.put("gamePlayers", gamePlayer.getGameID().getGamePlayer()
                .stream()
                .map(this::makeGamePlayersDTO)
                .collect(Collectors.toList()));
        dto.put("ships", gamePlayer.getShips()
                .stream()
                .map(this::makeShipsDTO)
                .collect(Collectors.toList()));
        dto.put("salvoes", gamePlayer.getGameID().getGamePlayers()
                .stream()
                .flatMap(gameplayer -> gameplayer.getSalvos().stream().map(this::makeSalvoesDTO)));
        if(gameState(gamePlayer) == "WAITINGFOROPP"){
            dto.put("hits", makeNoHitsDTO());
        }
        else {
            dto.put("hits", makeHitsDTO(gamePlayer));
        }
        return dto;
    }

    private Map<String, Object> makeShipsDTO(Ship ship) {
        Map<String, Object> dto = new LinkedHashMap<String, Object>();
        dto.put("type", ship.getType());
        dto.put("locations", ship.getShipLocations());
        return dto;
    }

    private Map<String, Object> makeSalvoesDTO(Salvo salvo) {
        Map<String, Object> dto = new LinkedHashMap<String, Object>();
        dto.put("turn", salvo.getTurnNumber());
        dto.put("player", salvo.getGamePlayerID().getPlayerID().getId());
        dto.put("locations", salvo.getSalvoLocations());
        return dto;
    }

    public Map<String, Object> makeScoreDTO(Score score) {

        Map<String, Object> dto = new LinkedHashMap<>();
        dto.put("player", score.getPlayerID().getId());
        dto.put("score", score.getScore());
        dto.put("finishDate", score.getFinishDate());
        return dto;

    }

    public Map<String, Object> makeHitsDTO(GamePlayer gamePlayer) {
        Map<String, Object> dto = new LinkedHashMap<>();
        dto.put("self",getEnemyPlayer(gamePlayer).getSalvos().stream().map(this::makeHitsInfoDTO).collect(Collectors.toList()));
        dto.put("opponent",gamePlayer.getSalvos().stream().map(this::makeHitsInfoDTO).collect(Collectors.toList()));
        return dto;
    }

    public Map<String, Object> makeNoHitsDTO() {
        Map<String, Object> dto = new LinkedHashMap<>();
        dto.put("self",new ArrayList<>());
        dto.put("opponent",new ArrayList<>());
        return dto;
    }

    public Map<String, Object> makeHitsInfoDTO(Salvo salvo) {
        Map<String, Object> dto = new LinkedHashMap<>();
        dto.put("turn", salvo.getTurnNumber());
        dto.put("hitLocations", getHits(salvo));
        dto.put("damages", damageShip(salvo.getGamePlayerID(),salvo));
        dto.put("missed", (salvo.getSalvoLocations().size() - getHits(salvo).size()));
        return dto;
    }


    //Crear un nuevo Player
    @RequestMapping(path = "/players", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> register(
            @RequestParam(value = "email") String username, @RequestParam(value = "password") String password) {

        if (username.isEmpty() || password.isEmpty()) {
            return new ResponseEntity<>(makeMap("error", "Missing data"), HttpStatus.FORBIDDEN);
        } else if (playerRepository.findByUserName(username) != null) {
            return new ResponseEntity<>(makeMap("error", "Name already in use"), HttpStatus.UNAUTHORIZED);
        } else {
            playerRepository.save(new Player(username, passwordEncoder.encode(password)));
            return new ResponseEntity<>(makeMap("Success", username), HttpStatus.CREATED);
        }
    }

    //Crear un juego nuevo
    @PostMapping("/games")
    public ResponseEntity<Map<String, Object>> createGame(Authentication authentication) {

        ResponseEntity<Map<String, Object>> newGame;

        if (isGuest(authentication)) {
            newGame = new ResponseEntity<>(makeMap("error", "UNAUTHORIZED: player no login"), HttpStatus.UNAUTHORIZED);
        } else {

            //Hago lo mismo que para crear un player pero pasandole los parametros para crear un GamePlayer: joinDate, Player,
            // y creationDate.

            Date joinDate = new Date();
            Player ownnerPlayer = playerRepository.findByUserName(authentication.getName());
            Game newCreationDate = gameRepository.save(new Game(joinDate));

            GamePlayer newGamePlayer = gamePlayerRepository.save(new GamePlayer(joinDate, ownnerPlayer
                    , newCreationDate));

            newGame = new ResponseEntity<>(makeMap("gpid", newGamePlayer.getId()), HttpStatus.CREATED);
        }
        return newGame;
    }

    //Unirse a un juego
    @PostMapping("/game/{gameid}/players")
    public ResponseEntity<Map<String, Object>> joinGame(@PathVariable Long gameid, Authentication authentication) {

        ResponseEntity<Map<String, Object>> joinGame;
        Optional<Game> game = gameRepository.findById(gameid);


        //Tengo que validar: 1-Si esta logueado / 2-Si el game existe / 3-Si el game tiene un solo player / 4- Que el player 1 no sea igual al player 2
        //5- Crear nuevo GP
        if (isGuest(authentication)) {
            joinGame = new ResponseEntity<>(makeMap("error", "UNAUTHORIZED: player no login"), HttpStatus.UNAUTHORIZED);
        } else if (game.isEmpty()) {
            joinGame = new ResponseEntity<>(makeMap("error", "game doesnt exist"), HttpStatus.FORBIDDEN);
        } else if (game.get().getPlayers().size() > 1) {
            joinGame = new ResponseEntity<>(makeMap("error", "game full"), HttpStatus.FORBIDDEN);
        } else if (Objects.equals(game.get().getGamePlayers().stream().findFirst().get().getPlayerID().getId(), playerRepository.findByUserName(authentication.getName()).getId())) {
            joinGame = new ResponseEntity<>(makeMap("error", "you are already joined to that game"), HttpStatus.FORBIDDEN);
        }
        //Hago lo mismo que para crear un player pero pasandole los parametros para crear un GamePlayer: joinDate, Player,
        // y creationDate.
        else {
            Date joinDate = new Date();
            GamePlayer newGamePlayer = gamePlayerRepository.save(new GamePlayer(joinDate, playerRepository.save(playerRepository.findByUserName(authentication.getName()))
                    , game.get()));
            //joinGame = new ResponseEntity<>(makeMap("Join at game", + game.get().getId()),HttpStatus.CREATED);
            joinGame = new ResponseEntity<>(makeMap("gpid", newGamePlayer.getId()), HttpStatus.CREATED);
        }
        return joinGame;
    }

    //Posicionar Barcos
    @PostMapping("/games/players/{gamePlayerId}/ships")
    public ResponseEntity<Map<String, Object>> placeShip(@PathVariable Long gamePlayerId, Authentication authentication, @RequestBody List<Ship> ships) {

        ResponseEntity<Map<String, Object>> shipPlace;
        Optional<GamePlayer> gamePlayer = gamePlayerRepository.findById(gamePlayerId);

        if (isGuest(authentication)) { //Verifico si esta logeado
            shipPlace = new ResponseEntity<>(makeMap("error", "UNAUTHORIZED: player no login"), HttpStatus.UNAUTHORIZED);
        } else if (gamePlayer.isEmpty()) { //Verifico si el gamePlayer existe
            shipPlace = new ResponseEntity<>(makeMap("error", "GamePlayer doesnt exist"), HttpStatus.FORBIDDEN);
        } else if (preventCheat(gamePlayer.get(), playerRepository.findByUserName(authentication.getName()))) { //Verifico si el id del player de GamePlayer es igual al del Player logueado
            shipPlace = new ResponseEntity<>(makeMap("error", "Player is UNAUTHORIZED"), HttpStatus.FORBIDDEN);
        } else if (gamePlayer.get().getShips().size() != 0) {
            shipPlace = new ResponseEntity<>(makeMap("error", "Player alredy ship placed"), HttpStatus.FORBIDDEN);
        } else {
            if (ships.size() == 5) {
                //Tengo que pasarle: shipType, gamePlayer, Locations - shipType y location vienen de request body
                for (Ship ship : ships) {
                    gamePlayer.get().addShip(ship);
                    shipRepository.save(ship);
                    //Sin Metodo
                    //Ship shipeta = new Ship(ship.getType(),gamePlayer.get(),ship.getLocations());
                    //shipRepository.save(shipeta);
                }
                shipPlace = new ResponseEntity<>(makeMap("OK", "Created"), HttpStatus.ACCEPTED);
            } else {
                shipPlace = new ResponseEntity<>(makeMap("error", "You dont send ships"), HttpStatus.ACCEPTED);
            }
        }
        return shipPlace;
    }

    //Ver lista de Barcos posicionados
    @GetMapping("/games/players/{gamePlayerId}/ships")
    public ResponseEntity<Map<String, Object>> placeShip(@PathVariable Long gamePlayerId, Authentication authentication) {

        ResponseEntity<Map<String, Object>> shipPlace;
        Optional<GamePlayer> gamePlayer = gamePlayerRepository.findById(gamePlayerId);

        if (isGuest(authentication)) { //Verifico si esta logeado
            shipPlace = new ResponseEntity<>(makeMap("error", "UNAUTHORIZED: player no login"), HttpStatus.UNAUTHORIZED);
        } else if (gamePlayer.isEmpty()) { //Verifico si el gamePlayer existe
            shipPlace = new ResponseEntity<>(makeMap("error", "GamePlayer doesnt exist"), HttpStatus.FORBIDDEN);
        } else if (preventCheat(gamePlayer.get(), playerRepository.findByUserName(authentication.getName()))) { //Verifico si el id del player de GamePlayer es igual al del Player logueado
            shipPlace = new ResponseEntity<>(makeMap("error", "Player is UNAUTHORIZED"), HttpStatus.FORBIDDEN);
        } else if (gamePlayer.get().getShips().isEmpty()) {
            shipPlace = new ResponseEntity<>(makeMap("error", "no ships placed"), HttpStatus.FORBIDDEN);
        } else {
            shipPlace = new ResponseEntity<>(makeMap("Current Ships of GamePlayer " + gamePlayer.get().getId(), gamePlayer.get().getShips().stream().map(this::makeShipsDTO)), HttpStatus.ACCEPTED);
        }
        return shipPlace;
    }

    //Disparar Salvos
    @PostMapping("/games/players/{gamePlayerId}/salvoes")
    public ResponseEntity<Map<String, Object>> fireSalvos(@PathVariable Long gamePlayerId, Authentication authentication, @RequestBody Salvo salvo) {

        ResponseEntity<Map<String, Object>> salvoPlace;
        Optional<GamePlayer> gamePlayer = gamePlayerRepository.findById(gamePlayerId);


        if (isGuest(authentication)) { //Verifico si esta logeado
            salvoPlace = new ResponseEntity<>(makeMap("error", "UNAUTHORIZED: player no login"), HttpStatus.UNAUTHORIZED);
        } else if (gamePlayer.isEmpty()) { //Verifico si el gamePlayer existe
            salvoPlace = new ResponseEntity<>(makeMap("error", "GamePlayer doesnt exist"), HttpStatus.FORBIDDEN);
        } else if (preventCheat(gamePlayer.get(), playerRepository.findByUserName(authentication.getName()))) { //Verifico si el id del player de GamePlayer es igual al del Player logueado
            salvoPlace = new ResponseEntity<>(makeMap("error", "Player is UNAUTHORIZED"), HttpStatus.FORBIDDEN);
        } else if (gamePlayer.get().getShips().isEmpty()) { //Verifico si el gamePlayer coloco sus barcos
            salvoPlace = new ResponseEntity<>(makeMap("error", "No ships placed"), HttpStatus.FORBIDDEN);
        } else if (gamePlayer.get().getGameID().getGamePlayers().size() < 2) { //Valido q haya 2 players en el juego
            salvoPlace = new ResponseEntity<>(makeMap("error", "Waiting for rival"), HttpStatus.FORBIDDEN);
        } else if (getEnemyPlayer(gamePlayer.get()).getShips().isEmpty()) { //Valido q el gameplayer enemigo haya colocado los barcos
            salvoPlace = new ResponseEntity<>(makeMap("error", "The enemy has not placed their ships"), HttpStatus.FORBIDDEN);
        } else if (gamePlayer.get().getId() < getEnemyPlayer(gamePlayer.get()).getId() && gamePlayer.get().getSalvos().size() != getEnemyPlayer(gamePlayer.get()).getSalvos().size()) { //Valido q sea el turno de tirar de este gameplayer
            salvoPlace = new ResponseEntity<>(makeMap("error", "Waiting for player 2's salvoes"), HttpStatus.FORBIDDEN);
        } else if (gamePlayer.get().getId() > getEnemyPlayer(gamePlayer.get()).getId() && gamePlayer.get().getSalvos().size() >= getEnemyPlayer(gamePlayer.get()).getSalvos().size()) { //Valido q sea el turno de tirar de este gameplayer
            salvoPlace = new ResponseEntity<>(makeMap("error", "Waiting for player 1's salvoes"), HttpStatus.FORBIDDEN);
        } else if (salvo.getSalvoLocations().size() < 1 || salvo.getSalvoLocations().size() > 5) { //Valido que se envien minimo 1 salvo y maximo 5
            salvoPlace = new ResponseEntity<>(makeMap("error", "The amount of salvos is incorrect"), HttpStatus.FORBIDDEN);
        } else {
            salvo.setTurnNumber(gamePlayer.get().getSalvos().size() + 1); //Asigno el turno
            gamePlayer.get().addSalvos(salvo); //Asigno el GP
            salvoRepository.save(salvo); //Guardo el Salvo

            salvoPlace = new ResponseEntity<>(makeMap("OK", "Salvos fired"), HttpStatus.ACCEPTED);
        }

        return salvoPlace;
    }

    //Lista de Salvos
    @GetMapping("/games/players/{gamePlayerId}/salvoes")
    public ResponseEntity<Map<String, Object>> fireSalvos(@PathVariable Long gamePlayerId, Authentication authentication) {

        ResponseEntity<Map<String, Object>> salvoPlace;
        Optional<GamePlayer> gamePlayer = gamePlayerRepository.findById(gamePlayerId);

        if (isGuest(authentication)) { //Verifico si esta logeado
            salvoPlace = new ResponseEntity<>(makeMap("error", "UNAUTHORIZED: player no login"), HttpStatus.UNAUTHORIZED);
        } else if (gamePlayer.isEmpty()) { //Verifico si el gamePlayer existe
            salvoPlace = new ResponseEntity<>(makeMap("error", "GamePlayer doesnt exist"), HttpStatus.FORBIDDEN);
        } else if (preventCheat(gamePlayer.get(), playerRepository.findByUserName(authentication.getName()))) { //Verifico si el id del player de GamePlayer es igual al del Player logueado
            salvoPlace = new ResponseEntity<>(makeMap("error", "Player is UNAUTHORIZED"), HttpStatus.FORBIDDEN);
        } else if (gamePlayer.get().getSalvos().isEmpty()) {
            salvoPlace = new ResponseEntity<>(makeMap("error", "No salvoes for this gameplayer"), HttpStatus.FORBIDDEN);
        } else {
            salvoPlace = new ResponseEntity<>(makeMap("Current Ships of GamePlayer " + gamePlayer.get().getId(),
                    gamePlayer.get().getSalvos().stream().map(this::makeSalvoesDTO)),
                    HttpStatus.ACCEPTED);
        }

        return salvoPlace;
    }


    //Metodo para validar si ya se dispararon salvos en un turno determinado
    private boolean newSalvos(Salvo newSalvo, Set<Salvo> salvos) {
        boolean validTurn = true;
        for (Salvo salvo : salvos) {
            if (newSalvo.getTurnNumber() == salvo.getTurnNumber()) {
                validTurn = false;
            }
        }
        return validTurn;
    }

    //Metodo para validar si el GP corresponde al Player logueado
    private boolean preventCheat(GamePlayer gamePlayer, Player player) {
        boolean wrongGp = gamePlayer.getPlayerID().getId() != player.getId();
        return wrongGp;
    }

    //Metodo para obtener el gameplayer enemigo
    private GamePlayer getEnemyPlayer(GamePlayer gamePlayer) {
        Long gamePlayerId = gamePlayer.getId();
        Set<GamePlayer> gamePlayers = gamePlayer.getGameID().getGamePlayers();
        GamePlayer enemyGamePlayer = gamePlayers.stream().filter(gp -> gp.getId() != gamePlayerId).findFirst().orElse(null);
        return enemyGamePlayer;
    }

    //Metodo IsGuest
    private boolean isGuest(Authentication authentication) {
        return authentication == null || authentication instanceof AnonymousAuthenticationToken;
    }

    //Metodo MakeMap
    private Map<String, Object> makeMap(String key, Object value) {
        Map<String, Object> map = new HashMap<>();
        map.put(key, value);
        return map;
    }

    //Metodo Obtener Hits
    private List<String> getHits(Salvo salvo) {
        List<String> salvoLocations = salvo.getSalvoLocations();
        List<String> enemySalvoLocations = getEnemyPlayer(salvo.getGamePlayerID()).getShips().stream().map(x -> x.getShipLocations()).flatMap(y -> y.stream()).collect(Collectors.toList());

        List<String> hits = salvoLocations.stream().filter(z -> enemySalvoLocations.contains(z)).collect(Collectors.toList());
        return hits;
    }

    //Metodo para Calcular los Hits de Cada Ship
    private Map<String, Object> damageShip(GamePlayer gamePlayer, Salvo salvoTurn) {
        Map<String, Object> shipDamage = new HashMap<>();

        //Variables daños totales
        int patrol = 0;
        int submarine = 0;
        int destroyer = 0;
        int battleship = 0;
        int carrier = 0;

        //Variables daños por turno
        int patrol1 = 0;
        int submarine1 = 0;
        int destroyer1 = 0;
        int battleship1 = 0;
        int carrier1 = 0;

        //Lista salvos acumulados segun turno
        List<Salvo> salvos = gamePlayer.getSalvos().stream().filter(x->x.getTurnNumber() <= salvoTurn.getTurnNumber()).collect(Collectors.toList());

        //Lista total de ubicacion de salvos
        List<String> salvoes= salvos.stream().map(x->x.getSalvoLocations()).flatMap(y->y.stream()).collect(Collectors.toList());

        //Lista hits turno
        List<String> salvoesTurn = getHits(salvoTurn);

        //For para calcular daños por barco
        for(Ship ship : getEnemyPlayer(gamePlayer).getShips()) {
            for (String locations : ship.getShipLocations()) {

                switch (ship.getType()) {
                    case "battleship":
                        if (salvoes.contains(locations)) {
                            battleship++;
                            shipDamage.put("battleship", battleship);
                        } else {
                            shipDamage.put("battleship", battleship);
                        }
                        if (salvoesTurn.contains(locations)) {
                            battleship1++;
                            shipDamage.put("battleshipHits", battleship1);
                        } else {
                            shipDamage.put("battleshipHits", battleship1);
                        }
                        break;

                        case "patrolboat":
                            if(salvoes.contains(locations)) {
                                patrol++;
                                shipDamage.put("patrolboat",patrol);
                            }
                            else{
                                shipDamage.put("patrolboat",patrol);
                            }
                            if(salvoesTurn.contains(locations)) {
                                patrol1++;
                                shipDamage.put("patrolboatHits",patrol1);
                            }
                            else{
                                shipDamage.put("patrolboatHits",patrol1);
                            }
                            break;

                        case "carrier":
                            if(salvoes.contains(locations)) {
                                carrier++;
                                shipDamage.put("carrier", carrier);
                            }
                            else{
                                shipDamage.put("carrier",carrier);

                            }
                            if(salvoesTurn.contains(locations)) {
                                carrier1++;
                                shipDamage.put("carrierHits", carrier1);
                            }
                            else{
                                shipDamage.put("carrierHits",carrier1);
                            }
                            break;

                        case "submarine":
                            if(salvoes.contains(locations)) {
                                submarine++;
                                shipDamage.put("submarine", submarine);
                            }
                            else{
                                shipDamage.put("submarine",submarine);
                            }
                            if(salvoesTurn.contains(locations)) {
                                submarine1++;
                                shipDamage.put("submarineHits", submarine1);
                            }
                            else{
                                shipDamage.put("submarineHits",submarine1);
                            }
                            break;

                        case "destroyer":
                            if(salvoes.contains(locations)) {
                                destroyer++;
                                shipDamage.put("destroyer", destroyer);
                            }
                            else{
                                shipDamage.put("destroyer",destroyer);
                            }
                            if(salvoesTurn.contains(locations)) {
                                destroyer1++;
                                shipDamage.put("destroyerHits", destroyer1);
                            }
                            else{
                                shipDamage.put("destroyerHits",destroyer1);
                            }
                            break;
                    }
                }
            }

        return shipDamage;
    }

    //Metodo para definir el estado del juego
    private String gameState(GamePlayer gamePlayer){
        String gameState = new String();
        if(gamePlayer.getGameID().getGamePlayers().size() < 2) {
            gameState ="WAITINGFOROPP";
        }else if(gamePlayer.getShips().size() == 0 || gamePlayer.getShips().size() < 5){
            gameState ="PLACESHIPS";
        }else if(getEnemyPlayer(gamePlayer).getShips().size() == 0 || getEnemyPlayer(gamePlayer).getShips().size() < 5){
            gameState ="WAITING";
        }else if(gamePlayer.getSalvos().size() == getEnemyPlayer(gamePlayer).getSalvos().size()) {
            boolean playerSunk = shipsSunk(gamePlayer);
            boolean enemySunk = shipsSunk(getEnemyPlayer(gamePlayer));
            if (playerSunk == true && enemySunk == false) {
                gameState ="LOST";
            }
            if (playerSunk == false && enemySunk == true) {
                gameState ="WON";
            }
            if (playerSunk == true && enemySunk == true) {
                gameState ="TIE";
            }
            if (playerSunk == false && enemySunk == false) {
                if (gamePlayer.getId() < getEnemyPlayer(gamePlayer).getId()) {
                    gameState ="PLAY";
                } else {
                    gameState ="WAIT";
                }
            }
        }else if (gamePlayer.getId() < getEnemyPlayer(gamePlayer).getId() && gamePlayer.getSalvos().size() != getEnemyPlayer(gamePlayer).getSalvos().size()) {
            gameState ="WAIT";
        }else if (gamePlayer.getId() > getEnemyPlayer(gamePlayer).getId() && gamePlayer.getSalvos().size() < getEnemyPlayer(gamePlayer).getSalvos().size()) {
            gameState ="PLAY";
        }else if (gamePlayer.getId() > getEnemyPlayer(gamePlayer).getId() && gamePlayer.getSalvos().size() >= getEnemyPlayer(gamePlayer).getSalvos().size()) {
            gameState ="WAIT";
        }

        return gameState;
    }

    //Metodo para validar si todos los barcos fueros hundidos
    private Boolean shipsSunk(GamePlayer gamePlayer) {

            Set<Salvo> enemySalvos = new LinkedHashSet<>(getEnemyPlayer(gamePlayer).getSalvos());
            List<String> enemySalvoLocs = enemySalvos.stream().flatMap(salvo -> salvo.getSalvoLocations().stream()).collect(Collectors.toList());

            List<String> ships = gamePlayer.getShips().stream().flatMap(x->x.getShipLocations().stream()).collect(Collectors.toList());


            boolean sunk = enemySalvoLocs.containsAll(ships);

            if(!sunk) {
                return false;
            }

        return true;
    }

    //Metodo para setear Scores
    private void setScores (GamePlayer gamePlayer, String gameState){

        Date finishDate = new Date();
        Game game = gamePlayer.getGameID();
        Player player = gamePlayer.getPlayerID();
        Player enemy = getEnemyPlayer(gamePlayer).getPlayerID();

        switch (gameState){
            case "WON":
                if(gamePlayer.getGameID().getScore().size() == 0){
                    Score score = new Score(1,finishDate,game,player);
                    Score score1 = new Score(0,finishDate,game,enemy);
                    scoreRepository.save(score);
                    scoreRepository.save(score1);
                    break;
                }
            case "LOSE":
                if(gamePlayer.getGameID().getScore().size() == 0){
                    Score score = new Score(0,finishDate,game,player);
                    Score score1 = new Score(1,finishDate,game,enemy);
                    scoreRepository.save(score);
                    scoreRepository.save(score1);
                    break;
                }
            case "TIE":
                if(gamePlayer.getGameID().getScore().size() == 0){
                    Score score = new Score(0.5,finishDate,game,player);
                    Score score1 = new Score(0.5,finishDate,game,enemy);
                    scoreRepository.save(score);
                    scoreRepository.save(score1);
                    break;
                }
            default:
                break;

        }





    }

}

