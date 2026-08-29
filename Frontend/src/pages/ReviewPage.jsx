import React,{useEffect,useState} from 'react';
import {useLocation,useNavigate} from 'react-router-dom';
import {api} from '../api';
import '../styles/review.scss';

const type=q=>(q.type||'').toLowerCase();
const isMcq=q=>type(q).includes('mcq')||q.optionA!=null;
const isTrueFalse=q=>type(q).includes('true');
const isShortAnswer=q=>type(q).includes('short');
const isFillBlank=q=>type(q).includes('fill')||type(q).includes('blank');

const getUserAnswer=q=>{
  if(isMcq(q)){
    if(!q.selectedLetter) return 'No Answer';
    return q.selectedLetter+' - '+(q['option'+q.selectedLetter]||'');
  }
  return q.selectedText||'No Answer';
};
const getCorrectAnswer=q=>{
  if(isMcq(q)) return q.correctLetter+' - '+(q['option'+q.correctLetter]||'');
  return q.correctText||'';
};
const getStatus=q=>q.correct?'✔ Correct':'✖ Incorrect';
const getStatusClass=q=>q.correct?'correct-text':'wrong-text';
const getQuestionType=q=>{
  if(isMcq(q)) return 'Multiple Choice';
  if(isTrueFalse(q)) return 'True / False';
  if(isFillBlank(q)) return 'Fill in the Blank';
  if(isShortAnswer(q)) return 'Short Answer';
  return q.type;
};

export default function ReviewPage(){
  const {state={}}=useLocation(),nav=useNavigate();
  const [quiz,setQuiz]=useState([]);
  const [quizTitle,setQuizTitle]=useState('');
  const [score,setScore]=useState(0);
  const [totalQuestions,setTotalQuestions]=useState(0);
  const [correctAnswers,setCorrectAnswers]=useState(0);

  useEffect(()=>{
    const attemptId=state.attemptId;
    if(!attemptId) return;
    api.get('/review/'+attemptId).then(r=>{
      const d=r.data;
      setQuiz(d.questions||[]);
      setQuizTitle(d.quizTitle||'');
      setScore(d.scorePercent||0);
      setCorrectAnswers(d.correctAnswers||0);
      setTotalQuestions(d.totalQuestions||0);
    }).catch(()=>{});
    // eslint-disable-next-line react-hooks/exhaustive-deps
  },[]);

  return <div className="review-page">
    <section className="hero"><h1>Review Answers</h1><p>{quizTitle}</p></section>

    <section className="summary">
      <div className="summary-card"><h3>Score</h3><h2>{score}%</h2></div>
      <div className="summary-card"><h3>Correct</h3><h2>{correctAnswers}</h2></div>
      <div className="summary-card"><h3>Total</h3><h2>{totalQuestions}</h2></div>
    </section>

    <section className="questions">
      {quiz.map((q,i)=>
        <div className="question-card" key={i}>
          <div className="question-header">
            <div className="left"><span className="question-number">Question {i+1}</span><span className="question-type">{getQuestionType(q)}</span></div>
            <div className={'status '+getStatusClass(q)}>{getStatus(q)}</div>
          </div>
          <div className="question-body">
            <h3>{q.question}</h3>
            {isMcq(q)&&<div className="options">
              {['A','B','C','D'].map(l=>
                <div key={l} className={'option '+(q.correctLetter===l?'correct-option ':'')+(q.selectedLetter===l?'selected-option':'')}>
                  <strong>{l}.</strong> {q['option'+l]}
                </div>)}
            </div>}
            <div className="answer-grid">
              <div className="answer-box user"><h4>Your Answer</h4><p>{getUserAnswer(q)}</p></div>
              <div className="answer-box correct"><h4>Correct Answer</h4><p>{getCorrectAnswer(q)}</p></div>
            </div>
            <div className={'result '+getStatusClass(q)}>{getStatus(q)}</div>
          </div>
        </div>)}
    </section>

    <section className="actions">
      <button className="btn primary" onClick={()=>nav('/dashboard')}>Dashboard</button>
      <button className="btn secondary" onClick={()=>nav('/results')}>Back to Results</button>
    </section>

    {quiz.length===0&&<div className="empty-state">
      <h2>No Review Data Available</h2>
      <p>This quiz attempt could not be loaded.</p>
      <button className="btn primary" onClick={()=>nav('/dashboard')}>Go to Dashboard</button>
    </div>}
  </div>;
}
