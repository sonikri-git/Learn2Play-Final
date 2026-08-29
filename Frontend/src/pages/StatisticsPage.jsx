import React,{useEffect,useRef,useState} from 'react';
import {useNavigate} from 'react-router-dom';
import Navbar from '../components/Navbar';
import {api,currentUser} from '../api';
import {Chart,registerables} from 'chart.js';
import '../styles/statistics.scss';

Chart.register(...registerables);

export default function StatisticsPage(){
  const [s,setS]=useState(null);
  const [loading,setLoading]=useState(true);
  const nav=useNavigate();
  const lineRef=useRef(null),pieRef=useRef(null);
  const lineChart=useRef(null),pieChart=useRef(null);

  useEffect(()=>{
    const e=currentUser().email;
    if(!e){setLoading(false);return}
    api.get('/statistics/'+encodeURIComponent(e))
      .then(r=>{setS(r.data);setLoading(false)})
      .catch(()=>{setS({name:currentUser().name,email:e});setLoading(false)});
  },[]);

  useEffect(()=>{
    if(loading||!s) return;
    const t=setTimeout(()=>{
      if(lineRef.current){
        lineChart.current?.destroy();
        lineChart.current=new Chart(lineRef.current,{
          type:'line',
          data:{labels:s.dates||[],datasets:[{label:'Quiz Score',data:s.scores||[],borderColor:'#4B9509',backgroundColor:'rgba(75,149,9,.14)',fill:true,tension:.35}]},
          options:{responsive:true,plugins:{legend:{labels:{color:'#9AA5C0'}}},scales:{x:{ticks:{color:'#9AA5C0'}},y:{beginAtZero:true,max:100,ticks:{color:'#9AA5C0'}}}}
        });
      }
      if(pieRef.current){
        pieChart.current?.destroy();
        pieChart.current=new Chart(pieRef.current,{
          type:'doughnut',
          data:{labels:['Correct','Wrong'],datasets:[{data:[s.totalCorrectAnswers||0,s.totalWrongAnswers||0],backgroundColor:['#4B9509','#E23F57']}]},
          options:{responsive:true,plugins:{legend:{labels:{color:'#9AA5C0'}}}}
        });
      }
    },200);
    return ()=>{clearTimeout(t);lineChart.current?.destroy();pieChart.current?.destroy()};
  },[loading,s]);

  if(loading) return <><Navbar/><div className="statistics-page"><div className="loading">Loading Statistics...</div></div></>;
  if(!s) return <><Navbar/><div className="statistics-page"><div className="loading">Loading Statistics...</div></div></>;

  const cards=[
    ['📚',s.totalQuizzes,'Total Quizzes'],
    ['⭐',Number(s.averageScore||0).toFixed(1)+'%','Average Score'],
    ['🏆',Number(s.highestScore||0).toFixed(1)+'%','Highest Score'],
    ['📉',Number(s.lowestScore||0).toFixed(1)+'%','Lowest Score'],
    ['📄',s.uploadedDocuments,'Uploaded Files'],
    ['🎯',Number(s.accuracy||0).toFixed(1)+'%','Accuracy'],
    ['✅',s.totalCorrectAnswers,'Correct Answers'],
    ['❌',s.totalWrongAnswers,'Wrong Answers'],
    ['🔥',s.totalQuestionsAnswered,'Questions Answered'],
  ];

  return <><Navbar/><div className="statistics-page"><div className="statistics-container">
    <div className="header"><h1>📊 Learning Analytics</h1><h3>{s.name}</h3><p>{s.email}</p></div>

    <div className="stats-grid">
      {cards.map(([i,v,l])=><div className="card" key={l}><div className="icon">{i}</div><h2>{v??0}</h2><p>{l}</p></div>)}
    </div>

    <div className="charts">
      <div className="chart-card"><h2>📈 Score Progress</h2><canvas ref={lineRef} id="progressChart"/></div>
      <div className="chart-card"><h2>🥧 Correct vs Wrong</h2><canvas ref={pieRef} id="accuracyChart"/></div>
    </div>

    <div className="navigation">
      {[['🏠 Dashboard','/dashboard'],['📚 History','/history'],['🏆 Leaderboard','/leaderboard'],['👤 Profile','/profile']].map(([l,r])=>
        <button onClick={()=>nav(r)} key={r}>{l}</button>)}
    </div>
  </div></div></>;
}
