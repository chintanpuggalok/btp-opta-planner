package org.acme.schooltimetabling.utils;
import java.io.*;
import java.util.*;

public class ReadWrite {
   public static List<Set<String>> getBucketList()
    {
        String line = "";
        String splitBy = ",";
        BufferedReader br;
        try {
             br = new BufferedReader(new FileReader("subject_bucket_list.csv"));
            br.readLine();
            List<Set<String>> bucketList = new ArrayList<>();
            HashSet<String> bucket = new HashSet<>();
            List<HashSet<String>> seriesBuckets = new ArrayList<>();
            seriesBuckets.add(bucket);
            while ((line = br.readLine()) != null) // returns a Boolean value
            {
                String[] subjectDetails = line.split(splitBy);
                if(subjectDetails.length==1)
                {
                    // bucketList.add(bucket);
                    for(HashSet<String> set: seriesBuckets)
                    {
                        if(set.size()>0)
                        {

                            bucketList.add(set);
                        }
                    }
                    bucket = new HashSet<>();
                    seriesBuckets= new ArrayList<>();
                    seriesBuckets.add(bucket);
                    continue;
                }
                else if(subjectDetails.length>=2)
                {
                    if(subjectDetails.length==2)
                    {
                        for(HashSet<String> set: seriesBuckets)
                        {
                            set.add(subjectDetails[1]);
                        }
                    }
                    else
                    {
                        int multiplier=subjectDetails.length-1;
                        List<HashSet<String>> newSeriesBuckets = new ArrayList<>();
                        for(int i=0;i<multiplier;i++)
                        {
                            
                            for(HashSet<String> set: seriesBuckets)
                            {
                                newSeriesBuckets.add((HashSet<String>) set.clone());
                            }
                        }
                        // for(int i=0;i<multiplier;i++)
                        // {
                        //     for(int j=1;j<subjectDetails.length;j++)
                        //     {
                        //         newSeriesBuckets.get(i*multiplier+j-1 ).add(subjectDetails[j]);
                        //     }
                        // }
                        int sub=0;
                        for(int i=0;i<newSeriesBuckets.size();i++,sub++)
                        {
                            sub=sub%multiplier;
                            newSeriesBuckets.get(i).add(subjectDetails[sub+1]);
                        }
                        seriesBuckets=newSeriesBuckets;

                    }
                
            }
        }

            br.close();
            // return bucketList;
            return bucketList;
            
            
        } catch (Exception e) {
            
            System.out.println(e.getMessage());
            // TODO: handle exception
        }
        return null; 
        


    }

    
}
