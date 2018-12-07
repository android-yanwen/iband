package com.manridy.iband.bean;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by jarLiao on 18/2/3.
 */

public class WechatBean {

    /**
     * count : 20
     * result : [{"0":"40166","product_id":"40166","1":"Smart-2","device_name":"Smart-2","2":"c7599d89bc92","device_mac":"c7599d89bc92","3":"gh_42df2f04689e_d0f0d976900a3e58","device_id":"gh_42df2f04689e_d0f0d976900a3e58","4":"http://we.qq.com/d/AQBsp3K--3lc1uUzTXxr2KQqfvXVLtywD-q9jsh_","device_qr":"http://we.qq.com/d/AQBsp3K--3lc1uUzTXxr2KQqfvXVLtywD-q9jsh_","5":"1515660922","register_date":"1515660922","6":"33299","id":"33299"},{"0":"40166","product_id":"40166","1":"Smart-2","device_name":"Smart-2","2":"c7599d89bc92","device_mac":"c7599d89bc92","3":"gh_42df2f04689e_ff3c33f7280adad4","device_id":"gh_42df2f04689e_ff3c33f7280adad4","4":"http://we.qq.com/d/AQBsp3K-KOj_nqk5AI0E8lHmRiJaFKFiByS1TPf5","device_qr":"http://we.qq.com/d/AQBsp3K-KOj_nqk5AI0E8lHmRiJaFKFiByS1TPf5","5":"1515660733","register_date":"1515660733","6":"33292","id":"33292"},{"0":"40166","product_id":"40166","1":"Smart-2","device_name":"Smart-2","2":"c7599d89bc92","device_mac":"c7599d89bc92","3":"gh_42df2f04689e_178ce0e12aca2eb2","device_id":"gh_42df2f04689e_178ce0e12aca2eb2","4":"http://we.qq.com/d/AQBsp3K-1clM-xdD51n8ucXTfzUkRveZQanIczfr","device_qr":"http://we.qq.com/d/AQBsp3K-1clM-xdD51n8ucXTfzUkRveZQanIczfr","5":"1515660485","register_date":"1515660485","6":"33256","id":"33256"},{"0":"40166","product_id":"40166","1":"Smart-2","device_name":"Smart-2","2":"c7599d89bc92","device_mac":"c7599d89bc92","3":"gh_42df2f04689e_8a6defb5a6070126","device_id":"gh_42df2f04689e_8a6defb5a6070126","4":"http://we.qq.com/d/AQBsp3K-l3m1LwdpS50ILcO9vLAw4k0Q0nEqRE6u","device_qr":"http://we.qq.com/d/AQBsp3K-l3m1LwdpS50ILcO9vLAw4k0Q0nEqRE6u","5":"1515659951","register_date":"1515659951","6":"33210","id":"33210"},{"0":"40166","product_id":"40166","1":"Smart-2","device_name":"Smart-2","2":"c7599d89bc92","device_mac":"c7599d89bc92","3":"gh_42df2f04689e_d45caceaddced92b","device_id":"gh_42df2f04689e_d45caceaddced92b","4":"http://we.qq.com/d/AQBsp3K-kcQA9ST-nQ0VCzIuEgazPsIfaw8-LPrz","device_qr":"http://we.qq.com/d/AQBsp3K-kcQA9ST-nQ0VCzIuEgazPsIfaw8-LPrz","5":"1515659872","register_date":"1515659872","6":"33136","id":"33136"},{"0":"40166","product_id":"40166","1":"Smart-2","device_name":"Smart-2","2":"c7599d89bc92","device_mac":"c7599d89bc92","3":"gh_42df2f04689e_3488083c30ef3fa3","device_id":"gh_42df2f04689e_3488083c30ef3fa3","4":"http://we.qq.com/d/AQBsp3K-XLxuHVM9a_Lz0v0AjpeCzEhali4e0rFo","device_qr":"http://we.qq.com/d/AQBsp3K-XLxuHVM9a_Lz0v0AjpeCzEhali4e0rFo","5":"1515659872","register_date":"1515659872","6":"33122","id":"33122"},{"0":"40166","product_id":"40166","1":"Smart-2","device_name":"Smart-2","2":"c7599d89bc92","device_mac":"c7599d89bc92","3":"gh_42df2f04689e_275cf943c5c8e8f5","device_id":"gh_42df2f04689e_275cf943c5c8e8f5","4":"http://we.qq.com/d/AQBsp3K-SdNSJ4cFxKX0ucMf5uk7TWCzRX8raxIN","device_qr":"http://we.qq.com/d/AQBsp3K-SdNSJ4cFxKX0ucMf5uk7TWCzRX8raxIN","5":"1515658588","register_date":"1515658588","6":"33036","id":"33036"},{"0":"40166","product_id":"40166","1":"Smart-2","device_name":"Smart-2","2":"c7599d89bc92","device_mac":"c7599d89bc92","3":"gh_42df2f04689e_913c6a87fcf2c538","device_id":"gh_42df2f04689e_913c6a87fcf2c538","4":"http://we.qq.com/d/AQBsp3K-iRicqUrXmLyyZZEABz9YTJ-AKlAq90cU","device_qr":"http://we.qq.com/d/AQBsp3K-iRicqUrXmLyyZZEABz9YTJ-AKlAq90cU","5":"1515658588","register_date":"1515658588","6":"33020","id":"33020"},{"0":"40166","product_id":"40166","1":"Smart-2","device_name":"Smart-2","2":"c7599d89bc92","device_mac":"c7599d89bc92","3":"gh_42df2f04689e_bd1070e570e3187b","device_id":"gh_42df2f04689e_bd1070e570e3187b","4":"http://we.qq.com/d/AQBsp3K-pl_UTsKvFTfA5KBOYZJQ1PQOYXSBF5OU","device_qr":"http://we.qq.com/d/AQBsp3K-pl_UTsKvFTfA5KBOYZJQ1PQOYXSBF5OU","5":"1515584894","register_date":"1515584894","6":"32572","id":"32572"},{"0":"40166","product_id":"40166","1":"Smart-2","device_name":"Smart-2","2":"c7599d89bc92","device_mac":"c7599d89bc92","3":"gh_42df2f04689e_285f59512759e984","device_id":"gh_42df2f04689e_285f59512759e984","4":"http://we.qq.com/d/AQBsp3K-KasHNPqFdYhWbNzrawSMuLzyW5lG3O-k","device_qr":"http://we.qq.com/d/AQBsp3K-KasHNPqFdYhWbNzrawSMuLzyW5lG3O-k","5":"1515584894","register_date":"1515584894","6":"32566","id":"32566"},{"0":"40166","product_id":"40166","1":"Smart-2","device_name":"Smart-2","2":"c7599d89bc92","device_mac":"c7599d89bc92","3":"gh_42df2f04689e_65735a10234c0beb","device_id":"gh_42df2f04689e_65735a10234c0beb","4":"http://we.qq.com/d/AQBsp3K-b_VqZ9QtrcRxjL0cSQNNtFBv0D5wr-UQ","device_qr":"http://we.qq.com/d/AQBsp3K-b_VqZ9QtrcRxjL0cSQNNtFBv0D5wr-UQ","5":"1515068382","register_date":"1515068382","6":"26506","id":"26506"},{"0":"40166","product_id":"40166","1":"Smart-2","device_name":"Smart-2","2":"c7599d89bc92","device_mac":"c7599d89bc92","3":"gh_42df2f04689e_049eeda03105983d","device_id":"gh_42df2f04689e_049eeda03105983d","4":"http://we.qq.com/d/AQBsp3K-Cn5C6uiZ7K08nG29tMSbM-lGBIkYuGNv","device_qr":"http://we.qq.com/d/AQBsp3K-Cn5C6uiZ7K08nG29tMSbM-lGBIkYuGNv","5":"1515068324","register_date":"1515068324","6":"26481","id":"26481"},{"0":"40166","product_id":"40166","1":"Smart-2","device_name":"Smart-2","2":"c7599d89bc92","device_mac":"c7599d89bc92","3":"gh_42df2f04689e_bcc02d0921bfa560","device_id":"gh_42df2f04689e_bcc02d0921bfa560","4":"http://we.qq.com/d/AQBsp3K-MZivbb6dc-H43MTds1uQRRPYBd5WmyH7","device_qr":"http://we.qq.com/d/AQBsp3K-MZivbb6dc-H43MTds1uQRRPYBd5WmyH7","5":"1515068108","register_date":"1515068108","6":"26434","id":"26434"},{"0":"40166","product_id":"40166","1":"Smart-2","device_name":"Smart-2","2":"c7599d89bc92","device_mac":"c7599d89bc92","3":"gh_42df2f04689e_5ca69ec2a8a55804","device_id":"gh_42df2f04689e_5ca69ec2a8a55804","4":"http://we.qq.com/d/AQBsp3K-DuKmEBhlDp2FSlofl7vyNda9Pvp71tOv","device_qr":"http://we.qq.com/d/AQBsp3K-DuKmEBhlDp2FSlofl7vyNda9Pvp71tOv","5":"1515068108","register_date":"1515068108","6":"26428","id":"26428"},{"0":"40166","product_id":"40166","1":"Smart-2","device_name":"Smart-2","2":"c7599d89bc92","device_mac":"c7599d89bc92","3":"gh_42df2f04689e_abdcdde8c26fc8f1","device_id":"gh_42df2f04689e_abdcdde8c26fc8f1","4":"http://we.qq.com/d/AQBsp3K-84mLCFg-vc4H-CacUgkX83AfvIUGFOZS","device_qr":"http://we.qq.com/d/AQBsp3K-84mLCFg-vc4H-CacUgkX83AfvIUGFOZS","5":"1515068084","register_date":"1515068084","6":"26411","id":"26411"},{"0":"40166","product_id":"40166","1":"Smart-2","device_name":"Smart-2","2":"c7599d89bc92","device_mac":"c7599d89bc92","3":"gh_42df2f04689e_60ec046ab8906570","device_id":"gh_42df2f04689e_60ec046ab8906570","4":"http://we.qq.com/d/AQBsp3K-I1bXNNU-aobCJA_Tw0_U8TiPAZjUx-2a","device_qr":"http://we.qq.com/d/AQBsp3K-I1bXNNU-aobCJA_Tw0_U8TiPAZjUx-2a","5":"1515057509","register_date":"1515057509","6":"26262","id":"26262"},{"0":"40166","product_id":"40166","1":"Smart-2","device_name":"Smart-2","2":"c7599d89bc92","device_mac":"c7599d89bc92","3":"gh_42df2f04689e_60059f819f6655b5","device_id":"gh_42df2f04689e_60059f819f6655b5","4":"http://we.qq.com/d/AQBsp3K-eYDAdOhuoU8FReQC87e4-11rMEsYgTbK","device_qr":"http://we.qq.com/d/AQBsp3K-eYDAdOhuoU8FReQC87e4-11rMEsYgTbK","5":"1515057507","register_date":"1515057507","6":"26213","id":"26213"},{"0":"40166","product_id":"40166","1":"Smart-2","device_name":"Smart-2","2":"c7599d89bc92","device_mac":"c7599d89bc92","3":"gh_42df2f04689e_e81348ed1f910fa6","device_id":"gh_42df2f04689e_e81348ed1f910fa6","4":"http://we.qq.com/d/AQBsp3K-oALXfsmWywptd8ANysu6d59hPWf1luqz","device_qr":"http://we.qq.com/d/AQBsp3K-oALXfsmWywptd8ANysu6d59hPWf1luqz","5":"1515038426","register_date":"1515038426","6":"26131","id":"26131"},{"0":"40166","product_id":"40166","1":"Smart-2","device_name":"Smart-2","2":"c7599d89bc92","device_mac":"c7599d89bc92","3":"gh_42df2f04689e_51b0ec760e35a1c7","device_id":"gh_42df2f04689e_51b0ec760e35a1c7","4":"http://we.qq.com/d/AQBsp3K-65q7atMHu0BseIRe3lEzcaES4QH8_7R5","device_qr":"http://we.qq.com/d/AQBsp3K-65q7atMHu0BseIRe3lEzcaES4QH8_7R5","5":"1515038426","register_date":"1515038426","6":"26123","id":"26123"},{"0":"40166","product_id":"40166","1":"Smart-2","device_name":"Smart-2","2":"c7599d89bc92","device_mac":"c7599d89bc92","3":"gh_42df2f04689e_db047314c55d10c6","device_id":"gh_42df2f04689e_db047314c55d10c6","4":"http://we.qq.com/d/AQBsp3K-g1D_sUUhBah0W8qME6ZlZc-Acl_WJr8N","device_qr":"http://we.qq.com/d/AQBsp3K-g1D_sUUhBah0W8qME6ZlZc-Acl_WJr8N","5":"1508411383","register_date":"1508411383","6":"5358","id":"5358"}]
     */

    private int count;
    private List<ResultBean> result;

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public List<ResultBean> getResult() {
        return result;
    }

    public void setResult(List<ResultBean> result) {
        this.result = result;
    }

    public static class ResultBean {
        /**
         * 0 : 40166
         * product_id : 40166
         * 1 : Smart-2
         * device_name : Smart-2
         * 2 : c7599d89bc92
         * device_mac : c7599d89bc92
         * 3 : gh_42df2f04689e_d0f0d976900a3e58
         * device_id : gh_42df2f04689e_d0f0d976900a3e58
         * 4 : http://we.qq.com/d/AQBsp3K--3lc1uUzTXxr2KQqfvXVLtywD-q9jsh_
         * device_qr : http://we.qq.com/d/AQBsp3K--3lc1uUzTXxr2KQqfvXVLtywD-q9jsh_
         * 5 : 1515660922
         * register_date : 1515660922
         * 6 : 33299
         * id : 33299
         */

        private String product_id;
        private String device_name;
        private String device_mac;
        private String device_id;
        private String device_qr;
        private String register_date;
        private String id;

        public String getProduct_id() {
            return product_id;
        }

        public void setProduct_id(String product_id) {
            this.product_id = product_id;
        }

        public String getDevice_name() {
            return device_name;
        }

        public void setDevice_name(String device_name) {
            this.device_name = device_name;
        }

        public String getDevice_mac() {
            return device_mac;
        }

        public void setDevice_mac(String device_mac) {
            this.device_mac = device_mac;
        }

        public String getDevice_id() {
            return device_id;
        }

        public void setDevice_id(String device_id) {
            this.device_id = device_id;
        }

        public String getDevice_qr() {
            return device_qr;
        }

        public void setDevice_qr(String device_qr) {
            this.device_qr = device_qr;
        }

        public String getRegister_date() {
            return register_date;
        }

        public void setRegister_date(String register_date) {
            this.register_date = register_date;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }
    }
}
