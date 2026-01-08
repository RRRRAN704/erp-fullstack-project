<script setup>

import { ref, onMounted, nextTick } from 'vue'

import {
  get1stAndToday,    
  past7Day,          
  past30Day,         
  pastWeek,         
  pastMonth,         
} from '@/utils/formValidate'


import {
  getContractNumberStatistics,
  getContractAmountStatistics,
  getOrderNumberStatistics,
  getTopEmployeeStatistics
} from '@/apis/chart'

import TitleIndex from '@/views/Data/components/titleIndex.vue'        
import ContractCount from '@/views/Data/components/contractCountStatistics.vue' 
import ContractAmount from '@/views/Data/components/contractAmountStatistics.vue' 
import OrderCount from '@/views/Data/components/orderCountStatistics.vue' 
import TopEmployee from '@/views/Data/components/topEmployeeStatistics.vue'                   


const flag = ref(2)                 //  The default selected time tag (2 represents the past 7 days)
const tateData = ref([])            // Time data array
const contractCountData = ref({})       
const contractAmountData = ref({})            
const orderData = ref({})
const topEmployeeData = ref({})           

// Initialise data function
const init = (begin, end) => {
  // Use nextTick to ensure execution after DOM updates are complete.
  nextTick(() => {
    getContractNumberStatisticsData(begin, end)  
    getContractAmountStatisticsData(begin, end)      
    getOrderAmountStatisticsData(begin, end)    
    getTopEmployeeStatisticsData(begin, end)               
  })
}

// Obtain contract number statistics
const getContractNumberStatisticsData = async (begin, end) => {
  try {
    const res = await getContractNumberStatistics({ begin,end })
    const responseData = res.data
    
    // Process the returned data and convert the comma-separated string into an array.
    contractCountData.value = {
      dateList: responseData.dateList.split(','),         
      numberOfContractList: responseData.numberOfContractList.split(',')  
    }
  } catch (error) {
    console.error('Fail getting contract count data:', error)
  }
}

// Obtain statistical data on contract amounts
const getContractAmountStatisticsData = async (begin, end) => {
  try {
    const res = await getContractAmountStatistics({ begin,end })
    const responseData = res.data
    
    
    contractAmountData.value = {
      dateList: responseData.dateList.split(','),         
      contractAmountList: responseData.contractAmountList.split(',')  
    }
  } catch (error) {
    console.error('Fail getting contract amount data:', error)
  }
}

// Get order count statistics
const getOrderAmountStatisticsData = async (begin, end) => {
  try {
    const res = await getOrderNumberStatistics({ begin,end })
    const responseData = res.data
    


  orderData.value = {
      dateList: responseData.dateList.split(','),         
      orderNumberList: responseData.orderNumberList.split(',')  
    }
  } catch (error) {
    console.error('Fail getting order count data:', error)
  }

   
}

// Obtain data on the top 5 employees
const getTopEmployeeStatisticsData = async (begin, end) => {
  try {
    const res = await getTopEmployeeStatistics({ begin,end })
    const responseData = res.data
    
 
   topEmployeeData.value = {
      nameList: responseData.nameList.split(','),         
      amountList: responseData.amountList.split(',')  
    }
  } catch (error) {
    console.error('Fail getting top employee data:', error)
  }
}

// Get the time range corresponding to the currently selected time label.
const getTitleNum = (data) => {
  // Set the corresponding time range based on the incoming tag value.
  switch (data) {
    case 1:
      tateData.value = get1stAndToday()  
      break
    case 2:
      tateData.value = past7Day()        
      break
    case 3:
      tateData.value = past30Day()       
      break
    case 4:
      tateData.value = pastWeek()        
      break
    case 5:
      tateData.value = pastMonth()       
      break
    default:
      tateData.value = past7Day()        
  }
  
  // Update flag value
  flag.value = data
  
   // Reinitialise data based on the new time range.
  // tateData.value[0] is the start time, and tateData.value[1] is the end time.
  init(tateData.value[0], tateData.value[1])
}


onMounted(() => {
  //Default retrieval of data from the past 7 days
  getTitleNum(2)
})
</script>



<template>
  <!-- Dashboard Container -->
  <div class="dashboard-container home">
   
    <TitleIndex 
      @sendTitleInd="getTitleNum" 
      :flag="flag" 
      :tateData="tateData" 
    />
    
    <!-- Four-cell layout container -->
    <div class="grid-container">
      
      <div class="grid-row">
        
        <div class="grid-item">
          <ContractCount :contractCountData="contractCountData" />
        </div>
        
      
        <div class="grid-item">
          <ContractAmount :contractAmountData="contractAmountData" />
        </div>
      </div>
      
      
      <div class="grid-row">
    
        <div class="grid-item">
          <OrderCount :orderData="orderData" />
        </div>
        
       
        <div class="grid-item">
          <TopEmployee :topEmployeeData="topEmployeeData" />
        </div>
      </div>
    </div>
  </div>
</template>



<style lang="scss" scoped>
.dashboard-container {
  padding: 20px;
}

.grid-container {
  width: 100%;
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.grid-row {
  display: flex;
  gap: 20px;
  width: 100%;
}

.grid-item {
  flex: 1;
  min-height: 300px; 
  

  border: 1px solid #e8e8e8;
  border-radius: 8px;
  padding: 16px;
  background: #fff;
  

  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

</style>