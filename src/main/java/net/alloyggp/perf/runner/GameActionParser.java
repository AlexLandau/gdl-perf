package net.alloyggp.perf.runner;

import java.io.BufferedReader;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.google.common.collect.Queues;

public class GameActionParser {

    public static BlockingQueue<GameActionMessage> convert(BufferedReader in, TimeoutSignaler timeoutSignaler) {
        BlockingQueue<GameActionMessage> queue = Queues.newLinkedBlockingDeque(1000);

        Runnable runnable = () -> {
            try {
                while (true) {
                    String line = in.readLine();
                    if (line == null) {
                        //Insert "done" indicator in queue
                        queue.put(GameActionMessage.endOfMessages());
                        return;
                    }

                    if (line.startsWith(GameActionFormat.CHOSEN_MOVES_PREFIX)) {
                        queue.put(ChosenMovesMessage.parse(line));
                    } else if (line.startsWith(GameActionFormat.GOALS_PREFIX)) {
                        queue.put(GoalsMessage.parse(line));
                    } else if (line.startsWith(GameActionFormat.LEGAL_MOVES_PREFIX)) {
                        queue.put(LegalMovesMessage.parse(line));
                    } else if (line.startsWith(GameActionFormat.ROLES_PREFIX)) {
                        queue.put(RolesMessage.parse(line));
                    } else if (line.startsWith(GameActionFormat.TERMINAL_PREFIX)) {
                        queue.put(TerminalityMessage.parse(line));
                    }
                }
            } catch (Exception e) {
                GameActionMessage errorMessage = ErrorMessage.create(e);
                while (!queue.offer(errorMessage)) { /* repeat until successful */ }
                throw new RuntimeException(e);
            }
        };

        //Actually run the runnable...
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.submit(runnable);

        timeoutSignaler.onTimeout(() -> {
            executor.shutdownNow();
        });

        return queue;
    }

}
