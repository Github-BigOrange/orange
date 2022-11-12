package learning;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author 王志成
 * @date 2022年10月31日 10:36
 */
public class ThreadPoolTest {

    public static void main(String[] args) {

//        //创建线程池
//        ExecutorService executorService = Executors.newCachedThreadPool();
//        ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(10,30,3, TimeUnit.MINUTES,
//                new SynchronousQueue<>(), new ThreadPoolExecutor.AbortPolicy());

        int[] nums = {0};
        int massage = massage(nums);
        System.out.println(massage);
    }



    public static int massage(int[] nums) {
        if (nums.length>0){
//            int length = (int) Math.ceil(nums.length/2);

//            Map<Integer,Integer> map  = new LinkedHashMap<>(length);
//            for (int i = 0; i < nums.length; i++) {
//                map.put(i,nums[i]);
//            }
            int max = 0;
            List<Customer> customerList = getList(nums);
            List<Integer> indexList = new ArrayList<>();


            for (int i = 0; i < customerList.size(); i++) {
                int index = customerList.get(i).getIndex();
                if (!indexList.contains(index) && !indexList.contains(index + 1) && !indexList.contains(index - 1)) {
                    max += customerList.get(i).getValue();
                    indexList.add(index);
                }
            }
            return max;
        }else {
            return 0;
        }
    }


    public static List<Customer> getList(int[] arr){
        List<Customer> list = new ArrayList<>();

        for (int i = 0; i < arr.length; i++) {
            Customer customer = new Customer(i,arr[i]);
            list.add(customer);
        }
        list = list.stream().sorted(Comparator.comparing(Customer::getValue).reversed()).collect(Collectors.toList());
        return list;
    }


    public static int[] bubbleSortByClassic(int[] arr) {
        if (arr ==null || arr.length<2){
            return arr;
        }
        //记录一下最后一次元素交换的位置，那个位置也就是无序数列的边界，再往后就是有序区了。
        int lastExchangeIndex = 0;
        //无序数列的边界，每次比较只需要比到这里为止
        int sortBorder = arr.length - 1;

        for (int i = 0; i < arr.length-1; i++) {
            //有序标记，每一轮的初始是true
            boolean isSorted = true;
            for (int j = 0; j < sortBorder; j++) {
                if (arr[j]>arr[j+1]){
                    //进来说明有元素要交换，所以不是有序，标记为false
                    isSorted = false;
                    int temp = arr[j];
                    arr[j] = arr[j+1];
                    arr[j+1] = temp;
                    //更新交换位置
                    lastExchangeIndex = j;
                }
            }
            sortBorder = lastExchangeIndex;
            if (isSorted){
                break;
            }
        }
        return arr;
    }





}

class Customer{
    int index;
    int value;

    public Customer(int index, int value) {
        this.index = index;
        this.value = value;
    }

    public Customer() {
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }
}
