
<script setup>

import { ref, watch, nextTick, onMounted, onBeforeUnmount,shallowRef } from 'vue'

import * as echarts from 'echarts'


defineOptions({
  name: 'orderCountStatistics'  
})


const props = defineProps({
  orderData: {          
    type: Object,           
    required: true        
  }
})


const chartInstance = shallowRef(null)
const chartRef = ref(null)


const initChart = () => {
 
  const chartDom = chartRef.value
  if (!chartDom) return


  if (!chartInstance.value) {
    chartInstance.value = echarts.init(chartDom)
  }


  const option = {

    tooltip: {
      trigger: 'axis',     
      confine: true,
    },
    

    grid: {
      top: '5%',           
      left: '5%',          
      right: '5%',        
      bottom: '12%',       
      containLabel: false,  
    },
    

    xAxis: {
      type: 'category',    
      boundaryGap: true,  
      

      axisLabel: {
        margin: 20,
        textStyle: {
          color: '#666',    
          fontSize: '12px', 
        },
      },
      
 
      axisLine: {
        lineStyle: {
          color: '#E5E4E4', 
          width: 1,        
        },
      },
      
      data: props.orderData?.dateList || [],
    },
    

    yAxis: [
      {
        type: 'value',     
        min: 0,            
        

        axisLabel: {
          textStyle: {
            color: '#666',     
            fontSize: '12px',   
          }
        }
      }
    ],
    

    series: [
      {
        name: 'Order Count',      
        type: 'bar',    
        barWidth: '40%',     
               
    
        itemStyle: {
        color: "#6b6670",
        borderRadius: [4, 4, 0, 0], 
        },

        
        emphasis: {
          itemStyle: {
            color: "#FFC100",
          },
        },
        
  
        data: props.orderData?.orderNumberList || [],
      },
    ],
  }

  chartInstance.value.setOption(option)
}


watch(
  () => props.orderData,    
  () => {                    
    
    nextTick(() => {
      initChart()              
    })
  },
  { deep: true }           
)


onMounted(() => {
  
  nextTick(() => {
    initChart()               
  })

  window.addEventListener('resize', handleResize)
})

const handleResize = () => {
  chartInstance.value?.resize()
}


onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
  if (chartInstance.value) {
    chartInstance.value.dispose()  
    chartInstance.value = null       
  }
})

</script>





<template>

  <div class="container">

    <h2 class="homeTitle">Order Statistics</h2>
 
    <div class="charBox">
    
      <div ref ="chartRef" style="width: 100%; height: 320px"></div>
   
      <ul class="orderListLine turnover">
      
        <li>Number of Orders Received</li>
      </ul>
    </div>
  </div>
</template>



<style scoped>
.container {
  padding: 20px;
}

.homeTitle {
  font-size: 18px;
  font-weight: bold;
  margin-bottom: 20px;
  color: #333;
}

.charBox {
  background: #fff;
  border-radius: 8px;
  padding: 20px;
}

.orderListLine {
  list-style: none;
  padding: 0;
  margin: 10px 0 0 0;
}

.orderListLine li {
  padding: 8px 0;
  color: #666;
  font-size: 14px;
}

.turnover {
  border-top: 1px solid #eee;
}
</style>