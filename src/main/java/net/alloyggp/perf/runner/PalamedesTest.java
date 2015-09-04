package net.alloyggp.perf.runner;

import org.ggp.base.util.game.Game;
import org.ggp.base.util.game.GameRepository;

import stanfordlogic.game.GameManager;
import stanfordlogic.game.Gamer;
import stanfordlogic.gdl.GdlList;
import stanfordlogic.gdl.Parser;
import stanfordlogic.jocular.game.GenericGamerFactory;
import stanfordlogic.knowledge.BasicKB;
import stanfordlogic.knowledge.GameInformation;
import stanfordlogic.knowledge.KnowledgeBase;
import stanfordlogic.knowledge.MetaGdl;
import stanfordlogic.prover.AbstractReasoner;
import stanfordlogic.prover.BasicReasoner;
import stanfordlogic.prover.GroundFact;

public class PalamedesTest {

    public static void main(String[] args) throws Exception {
        Game game = GameRepository.getDefaultRepository().getGame("blockerSerial");
        String rulesheet = game.getRulesheet();
        rulesheet = rulesheet.substring(1, rulesheet.length() - 1);

        Parser parser = new Parser();
        GdlList rules = parser.parse(rulesheet);

        GenericGamerFactory factory = new GenericGamerFactory();
        String gameId = "fakeGameId";
        factory.makeGamer(gameId, role, description, startClock, playClock);

        Parser parser = GameManager.getParser();

        Gamer gamer = makeGamer(gameId, parser);

        GameInformation gameInfo = new MetaGdl(parser).examineGdl(description);

        KnowledgeBase staticKb = new BasicKB();
        staticKb.loadWithFacts(gameInfo.getAllGrounds());

        AbstractReasoner reasoner = new BasicReasoner(staticKb, gameInfo.getIndexedRules(), parser);

//        TermObject myRole = (TermObject) TermObject.buildFromGdl(role);

        gamer.initializeGame(myRole, playClock, gameInfo, reasoner);

        List<GroundFact> moves = getAllAnswers(currentContext_, "legal", myRole_.toString(), "?x");

//        GameSimulator simulator = new GameSimulator(false, false);
//        System.out.println(rulesheet);
//        File file = File.createTempFile("temp", ".kif");
//        GameFiles.write(game, file);
//        rulesheet = GameFiles.read(file);
//        System.out.println(rulesheet);
//        simulator.ParseDescIntoTheory(rulesheet);
//        System.out.println(simulator.GetRoles());
//        System.out.println(JavaEngineType.toRoles(simulator.GetRoles()));
//        simulator.SimulateStart();
//        Expression firstPlayer = simulator.GetRoles().get(0);
//        ExpList getLegalMoves = simulator.GetLegalMoves(firstPlayer);
//        System.out.println(getLegalMoves.get(0));
//        System.out.println(((Predicate)getLegalMoves.get(0)).getOperands().get(1));
//
//        List<Move> translatedMoves = ((List<?>)getLegalMoves.toArrayList()).stream()
//                .map(obj -> (Predicate) obj)
//                .map(predicate -> predicate.getOperands().get(1))
//                .map(Expression::toString)
//                .map(Move::create)
//                .collect(Collectors.toList());
//        System.out.println(translatedMoves);

    }

}
