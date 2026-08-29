import React,{useEffect,useState} from 'react';
import {useNavigate} from 'react-router-dom';
import Navbar from '../components/Navbar';
import {api,currentUser} from '../api';
import '../styles/history.scss';

const difficultyIcon=level=>({easy:'🟢',hard:'🔴',intermediate:'🟡'}[level]||'');
const titleCase=s=>s?s.charAt(0).toUpperCase()+s.slice(1).toLowerCase():'';
const formattedTimeTaken=item=>{
  const seconds=item?.timeTakenSeconds;
  if(typeof seconds!=='number') return null;
  const minutes=Math.floor(seconds/60),secs=seconds%60;
  return `${minutes}m ${String(secs).padStart(2,'0')}s`;
};

export default function HistoryPage(){
  const [history,setHistory]=useState([]),[loading,setLoading]=useState(true);
  const nav=useNavigate();
  useEffect(()=>{
    const e=currentUser().email;
    if(!e) return setLoading(false);
    api.get('/quiz/history/'+encodeURIComponent(e)).then(r=>setHistory(r.data||[])).catch(()=>{}).finally(()=>setLoading(false));
  },[]);

  return <><Navbar/><div className="history-page"><div className="history-container">
    <div className="header">
      <div><h1>📚 Quiz History</h1><p>View all your previous quiz attempts.</p></div>
      <button className="dashboard-btn" onClick={()=>nav('/dashboard')}>Dashboard</button>
    </div>

    {loading&&<div className="loading">Loading Quiz History...</div>}
    {!loading&&history.length===0&&<div className="empty"><h2>No Quiz Attempts Found</h2><p>Complete a quiz to see your history.</p></div>}

    {history.map((q,i)=>{
      const timeTaken=formattedTimeTaken(q);
      return <div className="history-card" key={q.attemptId||i}>
        <div className="left">
          <h2>📄 {q.quizTitle}
            {q.timedMode&&<span className="mode-badge">⏱️ Exam Mode</span>}
            {q.difficulty&&<span className={'difficulty-badge difficulty-'+q.difficulty}>{difficultyIcon(q.difficulty)} {titleCase(q.difficulty)}</span>}
          </h2>
          <p>Attempted On {q.attemptedAt?new Date(q.attemptedAt).toLocaleString():''}{timeTaken&&<span> • Time Taken: {timeTaken}</span>}</p>
          <div className="progress"><div className="progress-fill" style={{width:(q.scorePercent||0)+'%'}}/></div>
        </div>
        <div className="right">
          <div className="score">{Math.round(q.scorePercent||0)}%</div>
          <div className="correct">{q.correctCount} / {q.totalQuestions} Correct</div>
          <div className="buttons">
            <button className="review-btn" onClick={()=>nav('/review',{state:{attemptId:q.attemptId}})}>Review</button>
            <button className="leaderboard-btn" onClick={()=>nav('/leaderboard')}>🏆 Leaderboard</button>
          </div>
        </div>
      </div>;
    })}
  </div></div></>;
}
