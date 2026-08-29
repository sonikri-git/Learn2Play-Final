import React,{useEffect,useState} from 'react';
import {useLocation,useNavigate} from 'react-router-dom';
import {api} from '../api';
import '../styles/results.scss';

export default function ResultsPage(){
  const {state={}}=useLocation(),nav=useNavigate();
  const result=state.result||{};
  const autoSubmitted=!!state.autoSubmitted;
  const timeTakenSeconds=typeof state.timeTakenSeconds==='number'?state.timeTakenSeconds:null;

  const [reviewQuestions,setReviewQuestions]=useState([]);
  const [reviewLoaded,setReviewLoaded]=useState(false);
  const [reviewError,setReviewError]=useState(false);

  useEffect(()=>{
    const attemptId=result?.attemptId;
    if(!attemptId){setReviewError(true);return}
    api.get('/review/'+attemptId).then(r=>{
      setReviewQuestions(r.data.questions||[]);
      setReviewLoaded(true);
    }).catch(()=>setReviewError(true));
    // eslint-disable-next-line react-hooks/exhaustive-deps
  },[]);

  const formattedTimeTaken=timeTakenSeconds===null?null:(()=>{
    const m=Math.floor(timeTakenSeconds/60),s=timeTakenSeconds%60;
    return `${m}m ${String(s).padStart(2,'0')}s`;
  })();

  const score=result?.scorePercent||0;
  const percentage=Math.round(score);
  const totalQuestions=result?.totalQuestions||0;
  const correctAnswers=result?.correctCount||0;
  const wrongAnswers=totalQuestions-correctAnswers;

  const performanceTitle=percentage>=90?'Outstanding!':percentage>=75?'Excellent!':percentage>=60?'Good Job!':percentage>=40?'Keep Practicing':'Needs Improvement';
  const performanceMessage=percentage>=90?'You mastered this quiz.':percentage>=75?'Very strong performance.':percentage>=60?'You are doing well.':percentage>=40?'Review your mistakes.':'Practice again to improve.';
  const scoreClass=percentage>=90?'excellent':percentage>=75?'good':percentage>=60?'average':'poor';

  const getUserAnswer=q=>q.selectedText||q.selectedLetter||'No Answer';
  const getCorrectAnswer=q=>q.correctText||q.correctLetter||'';
  const isCorrect=q=>q.correct;
  const getQuestionType=q=>q.type||'Question';

  return <div className="results-page">
    <section className="hero">
      <div className="hero-content">
        <h1>🎉 Quiz Completed!</h1>
        <p>{performanceTitle}</p>
        <span className="subtitle">{performanceMessage}</span>
      </div>
    </section>

    <section className="score-card">
      <div className="score-circle">
        <svg width="220" height="220">
          <defs><linearGradient id="scoreGradient" x1="0%" y1="0%" x2="100%" y2="100%"><stop offset="0%" stopColor="#4B9509"/><stop offset="100%" stopColor="#4D6BFF"/></linearGradient></defs>
          <circle className="track" cx="110" cy="110" r="90"/>
          <circle className="progress" cx="110" cy="110" r="90" style={{strokeDashoffset:565-(565*percentage/100)}}/>
        </svg>
        <div className="score-text"><h1>{percentage}%</h1><p>Your Score</p></div>
      </div>
      <div className="score-summary">
        <div className="summary-item"><h2>{correctAnswers}</h2><span>Correct</span></div>
        <div className="summary-item"><h2>{wrongAnswers}</h2><span>Incorrect</span></div>
        <div className="summary-item"><h2>{totalQuestions}</h2><span>Total Questions</span></div>
      </div>
    </section>

    <section className="stats-grid">
      <div className="stat-box"><div className="icon">🏆</div><div><h3>Performance</h3><p>{percentage}%</p></div></div>
      <div className="stat-box"><div className="icon">✅</div><div><h3>Correct Answers</h3><p>{correctAnswers}</p></div></div>
      <div className="stat-box"><div className="icon">❌</div><div><h3>Wrong Answers</h3><p>{wrongAnswers}</p></div></div>
      <div className="stat-box"><div className="icon">📚</div><div><h3>Total Questions</h3><p>{totalQuestions}</p></div></div>
      {formattedTimeTaken&&<div className="stat-box"><div className="icon">⏱️</div><div><h3>Time Taken</h3><p>{formattedTimeTaken}</p></div></div>}
    </section>

    {autoSubmitted&&<p className="auto-submit-note">⏰ Time ran out — this quiz was submitted automatically.</p>}

    <section id="review-section" className="review-section">
      <h2>Review Answers</h2>
      {!reviewLoaded&&!reviewError&&<div className="review-loading">Loading detailed review…</div>}
      {reviewError&&<div className="review-error">Couldn't load the detailed review right now. Your score above is still accurate — try again from Quiz History in a moment.</div>}
      {reviewQuestions.map((q,i)=>
        <div className="review-card" key={i}>
          <div className="question-header">
            <span className="question-number">Question {i+1}</span>
            <span className={'badge '+scoreClass}>{getQuestionType(q)}</span>
          </div>
          <h3>{q.question}</h3>
          <div className="answer-box your-answer"><h4>Your Answer</h4><p>{getUserAnswer(q)}</p></div>
          <div className="answer-box correct-answer"><h4>Correct Answer</h4><p>{getCorrectAnswer(q)}</p></div>
          <div className={'status '+(isCorrect(q)?'correct':'incorrect')}>{isCorrect(q)?<span>✔ Correct</span>:<span>✖ Incorrect</span>}</div>
        </div>)}
    </section>

    <div className="actions">
      <button className="btn primary" onClick={()=>document.getElementById('review-section').scrollIntoView({behavior:'smooth'})}>Review Answers</button>
      <button className="btn secondary" onClick={()=>nav('/dashboard')}>Dashboard</button>
      <button className="btn secondary" onClick={()=>nav('/history')}>Quiz History</button>
      <button className="btn secondary" onClick={()=>nav('/quiz')}>Retake Quiz</button>
    </div>
  </div>;
}
