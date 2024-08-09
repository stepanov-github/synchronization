import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Main {
    public static final Map<Integer, Integer> sizeToFreq = new HashMap<>();
    private static final int THREADS = 1000;

    public static void main(String[] args) throws InterruptedException {

        final ExecutorService threadPool = Executors.newFixedThreadPool(THREADS);


        Thread thread = new Thread(() -> {
            System.out.printf("%s started... \n", Thread.currentThread().getName());
            synchronized (sizeToFreq) {
                while (!Thread.interrupted()) {
                    try {
                        sizeToFreq.wait();
                    } catch (InterruptedException e) {
                        return;
                    }
                    int max = 0;
                    for (Map.Entry<Integer, Integer> entry : sizeToFreq.entrySet()) {
                        max = Math.max(max, entry.getValue());
                    }
                    System.out.println("Текущий максимум: " + max);
                }

            }
            System.out.printf("%s finished... \n", Thread.currentThread().getName());
        });
        thread.start();

        Runnable logic = null;
        for (int j = 0; j < THREADS; j++) {
            logic = () -> {
                int r = 0;
                String str = generateRoute("RLRFR", 100);
                for (int i = 0; i < str.length(); i++) {
                    if (str.charAt(i) == 'R') {
                        r++;
                    }
                }
                synchronized (sizeToFreq) {
                    if (sizeToFreq.containsKey(r)) {
                        sizeToFreq.put(r, sizeToFreq.get(r) + 1);
                    } else {
                        sizeToFreq.put(r, 1);
                    }
                    sizeToFreq.notify();
                }
            };
            threadPool.submit(logic);
        }
        threadPool.shutdown();
        threadPool.awaitTermination(100, TimeUnit.SECONDS);
//        thread.join();


        int max = 0;
        int maxkey = 0;
        for (Map.Entry<Integer, Integer> entry : sizeToFreq.entrySet()) {
            if (entry.getValue() > max) {
                max = entry.getValue();
                maxkey = entry.getKey();
            }
        }
        System.out.printf("Самое частое количество повторений %d (встретилось %d раз) \n", maxkey, max);
        System.out.println("Другие размеры:");
        for (Map.Entry<Integer, Integer> entry : sizeToFreq.entrySet()) {
            if (entry.getValue() != max) {
                System.out.printf("- %d (%d раз) \n", entry.getKey(), entry.getValue());
            }
        }


        thread.interrupt();
    }

    public static String generateRoute(String letters, int length) {
        Random random = new Random();
        StringBuilder route = new StringBuilder();
        for (int i = 0; i < length; i++) {
            route.append(letters.charAt(random.nextInt(letters.length())));
        }
        return route.toString();
    }
}
