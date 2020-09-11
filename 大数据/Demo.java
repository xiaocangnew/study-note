
public class IpUtil {

    private List<String> keyList = new LinkedList<String>();   //保存每个小文件中次数出现最多的IP
    private int ipMaxNum = 0;   //次数出现最多的值
    private int callNum = 0;

    /**
     * 模拟生成大量的IP（1亿个）并批量写入到同一个大文件中
     */
    public void genIP2BigFile(File ipFile, long numberOfLine){
        BufferedWriter bw = null;
        FileWriter fw = null;
        long startTime = System.currentTimeMillis();
        try{
            fw = new FileWriter(ipFile,true);
            bw = new BufferedWriter(fw);

            SecureRandom random = new SecureRandom();
            for (int i = 0; i < numberOfLine; i++) {
                bw.write("10."+random.nextInt(255)+"."+random.nextInt(255)+"."+random.nextInt(255)+"\n");
                if((i+1) % 1000 == 0) {
                    // 每1000条批量写入文件中，提高效率
                    bw.flush();
                }
            }
            bw.flush();
            long endTime = System.currentTimeMillis();
            System.err.println((endTime - startTime) / 1000);
        }catch (Exception e) {
            e.printStackTrace();
        }finally{
            try{
                if(fw != null){
                    fw.close();
                }
            }catch (Exception e) {
                e.printStackTrace();
            }
            try{
                if(bw != null){
                    bw.close();
                }
            }catch (Exception e) {
                e.printStackTrace();
            }
        }
    }



    /**
     * check这个大文件是否已经存在
     */
    public void checkFileExists(File ipFile) {
        if (!ipFile.exists()) {
            try {
                ipFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
            gernBigFile(ipFile, 100000000);
            System.out.println(">>>>>> Ip file generated.");
        } else {
            System.out.println(">>>>>> Ip file already existed.");
        }
    }



    /**
     * 对IP求取hash值并取余数（比如1000），将hash值相同的IP放入到同一个小文件中，得到1000个小文件
     */
    public void splitFile(File ipFile,int splitFileCount){
        Map<Integer,BufferedWriter> bwMap = new HashMap<Integer,BufferedWriter>();//保存每个文件的流对象
        Map<Integer,List<String>> dataMap = new HashMap<Integer,List<String>>();//分隔文件用
        BufferedReader br = null;
        FileReader fr = null;
        BufferedWriter bw = null;
        FileWriter fw = null;
        long startTime = System.currentTimeMillis();
        try{
            fr = new FileReader(ipFile);
            br = new BufferedReader(fr);
            String ipLine = br.readLine();
            //先创建文件及流对象方便使用
            for(int i=0;i<splitFileCount;i++){
                File file = new File("/Users/ycaha/Desktop/tmpIps/"+ i + ".log");
                bwMap.put(i, new BufferedWriter(new FileWriter(file,true)));
                dataMap.put(i, new LinkedList<String>());
            }
            while(ipLine != null){
                // 对每个ip求取hash
                int hashCode = ipLine.hashCode();
                hashCode = hashCode < 0 ? -hashCode : hashCode;
                // 对hash值取余数，根据余数分配文件地址
                int fileNum = hashCode % splitFileCount;
                List<String> list = dataMap.get(fileNum);
                list.add(ipLine + "\n");
                if(list.size() % 1000 == 0){
                    BufferedWriter writer = bwMap.get(fileNum);
                    for(String line : list){
                        writer.write(line);
                    }
                    writer.flush();
                    list.clear();
                }
                ipLine = br.readLine();
            }
            for(int fn : bwMap.keySet()){
                List<String> list = dataMap.get(fn);
                BufferedWriter writer = bwMap.get(fn);
                for(String line : list){
                    writer.write(line);
                }
                list.clear();
                writer.flush();
                writer.close();
            }
            bwMap.clear();
            long endTime = System.currentTimeMillis();
            System.err.println((endTime - startTime) / 1000);
        }catch (Exception e) {
            e.printStackTrace();
        }finally{
            try{
                if(fr != null){
                    fr.close();
                }
            }catch (Exception e) {
                e.printStackTrace();
            }
            try{
                if(br != null){
                    br.close();
                }
            }catch (Exception e) {
                e.printStackTrace();
            }
            try{
                if(fw != null){
                    fw.close();
                }
            }catch (Exception e) {
                e.printStackTrace();
            }
            try{
                if(bw != null){
                    bw.close();
                }
            }catch (Exception e) {
                e.printStackTrace();
            }
        }
    }



    /**
     * 读取单个小文件split，并获取小文件中的出现次数最大的IP
     */
    public void read(File split) {
        Map<String,Integer> ipNumMap = new HashMap<String, Integer>();   //保存每个文件中的每个IP出现的次数
        //使用局部变量，不要使用全局变量，以免OOM
        callNum ++;
        BufferedReader br = null;
        FileReader fr = null;
        long startTime = System.currentTimeMillis();
        try{
            fr = new FileReader(split);
            br = new BufferedReader(fr);
            String ipLine = br.readLine();
            while(ipLine != null) {
                ipLine = ipLine.trim();
                if (ipNumMap.containsKey(ipLine)) {
                    Integer count = ipNumMap.get(ipLine);
                    count ++;
                    ipNumMap.replace(ipLine, count);
                } else {
                    ipNumMap.put(ipLine, 1);
                }
                ipLine = br.readLine();
            }
            Set<String> keys = ipNumMap.keySet();
            for (String key: keys) {
                int value = ipNumMap.get(key);
                if (value > ipMaxNum) {
                    ipMaxNum = value;
                    keyList.add(key);
                }
            }

            long endTime = System.currentTimeMillis();
            totalTime += (endTime - startTime);
        } catch (Exception e) {
            e.printStackTrace();
        } finally{
            try{
                if(fr != null){
                    fr.close();
                }
            }catch (Exception e) {
                e.printStackTrace();
            }
            try{
                if(br != null){
                    br.close();
                }
            }catch (Exception e) {
                e.printStackTrace();
            }
        }
        System.out.println(">>>>>> FileOrder: " + callNum + ", ipMaxNum: " + ipMaxNum + ", key: " + keyList.get(keyList.size()-1) );
    }

}


// 主类
public class TestMain {

    public static void main(String[] args) throws UnsupportedEncodingException {
        File ipFile = new File("/Users/ycaha/Desktop/ipAddr.log");
        IpUtil genIp = new IpUtil();
        genIp.checkFileExists(ipFile);
        long start = System.currentTimeMillis();
        genIp.splitFile(ipFile, 1000);
        File files = new File("/Users/ycaha/Desktop/tmpIps/");
        for (File split : files.listFiles()) {
            genIp.read(split);
        }
        long end = System.currentTimeMillis();
        System.out.println(">>>>>> The whole consumed time in seconds: " + (end - start) / 1000);
    }
}




