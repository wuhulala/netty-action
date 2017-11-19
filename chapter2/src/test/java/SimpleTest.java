import com.wuhulala.util.TimeWatcher;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author wuhulala
 * @version 1.0
 * @date 2017/11/17
 * @description 作甚的
 */
public class SimpleTest {

    // 包含打印操作
    //100000  658  1061(拆分循环) 1.6
    //1000000  5576  5073(拆分循环) 0.9

    // 纯加操作
    //100000 4 6 1-1.5
    //100000 15 27 1.8
    //10000000 33 66 2.0
    public static void main(String[] args) {

        final int N = 10000000;
        List<Integer> list = new ArrayList<>();

        for (int i = 0; i < N; i++) {
            list.add(i);
        }
        TimeWatcher watcher = new TimeWatcher();
        long sum1 = 0;
        long sum11 = 0;
        Iterator<Integer> it = list.iterator();
        while (it.hasNext()) {
            int i = it.next();
            sum11 += i;
            sum1 += i;
        }
        long firstInterval = watcher.interval();

        long sum2 = 0;
        long sum22 = 0;
        Iterator<Integer> it2 = list.iterator();

        while (it2.hasNext()) {
            int i = it2.next();
            sum2 += i;
        }

        it2 = list.iterator();

        while (it2.hasNext()) {
            int i = it2.next();
            sum22 += i;
        }
        long secondInterval = watcher.interval();

        System.out.println(firstInterval);
        System.out.println(secondInterval);
        System.out.println(1.0 * secondInterval / firstInterval);

    }
}
