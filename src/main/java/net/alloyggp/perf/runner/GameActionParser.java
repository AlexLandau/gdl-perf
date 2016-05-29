package net.alloyggp.perf.runner;

import java.io.BufferedReader;
import java.util.Optional;
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

                    Optional<GameActionMessage> message = convertLine(line);
                    if (message.isPresent()) {
                        queue.put(message.get());
                        if (message.get().isEndOfMessages()) {
                            return;
                        }
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
                    } else if (line.startsWith(GameActionFormat.TEST_FINISHED_PREFIX)) {
                        queue.put(GameActionMessage.endOfMessages());
                        return;
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

    public static Optional<GameActionMessage> convertLine(String line) {
        if (line.startsWith(GameActionFormat.CHOSEN_MOVES_PREFIX)) {
            return Optional.of(ChosenMovesMessage.parse(line));
        } else if (line.startsWith(GameActionFormat.GOALS_PREFIX)) {
            return Optional.of(GoalsMessage.parse(line));
        } else if (line.startsWith(GameActionFormat.LEGAL_MOVES_PREFIX)) {
            return Optional.of(LegalMovesMessage.parse(line));
        } else if (line.startsWith(GameActionFormat.ROLES_PREFIX)) {
            return Optional.of(RolesMessage.parse(line));
        } else if (line.startsWith(GameActionFormat.TERMINAL_PREFIX)) {
            return Optional.of(TerminalityMessage.parse(line));
        } else if (line.startsWith(GameActionFormat.TEST_FINISHED_PREFIX)) {
            return Optional.of(GameActionMessage.endOfMessages());
        }
        return Optional.empty();
    }

}
