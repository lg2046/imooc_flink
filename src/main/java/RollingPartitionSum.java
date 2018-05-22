//import org.apache.flink.api.common.functions.MapFunction;
//import org.apache.flink.api.java.tuple.Tuple2;
//import org.apache.flink.streaming.api.checkpoint.ListCheckpointed;
//
//import java.util.Collections;
//import java.util.List;
//
//
//public class RollingPartitionSum implements MapFunction<String, Tuple2<String, Integer>>, ListCheckpointed<Integer> {
//
//    private int sum = 0;
//
//    @Override
//    public Tuple2<String, Integer> map(String str) {
//        sum++;
//        return Tuple2.of(str, sum);
//    }
//
//    @Override
//    public List<Integer> snapshotState(long checkpointId, long timestamp) throws Exception {
//        return Collections.singletonList(sum);
//    }
//
//    @Override
//    public void restoreState(List<Integer> state) throws Exception {
//        for (int i : state) {
//            sum += i;
//        }
//    }
//}