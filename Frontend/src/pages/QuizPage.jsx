import React,{useEffect,useMemo,useRef,useState} from 'react';
import {useLocation,useNavigate} from 'react-router-dom';
import {api,currentUser} from '../api';
import '../styles/quiz.scss';

const SECONDS_PER_QUESTION=72;
const getType=q=>(q.type||'').toLowerCase();
const isMcq=q=>{const t=getType(q);return t.includes('mcq')||!!(q.options&&Object.keys(q.options).length>2)};
const isTrueFalse=q=>getType(q).includes('true');
const isShortAnswer=q=>{const t=getType(q);return t.includes('short')||t.includes('subjective')};
const isFillBlank=q=>{const t=getType(q);return t.includes('fill')||t.includes('blank')};
const difficultyIcon=level=>({easy:'🟢',hard:'🔴',intermediate:'🟡'}[level]||'');
const difficultyLabel=level=>level?level.charAt(0).toUpperCase()+level.slice(1):null;

export default function QuizPage(){
  const loc=useLocation(),nav=useNavigate();
  const [quiz,setQuiz]=useState([]);
  const [loading,setLoading]=useState(true);
  const [error,setError]=useState(null);
  const [difficulty,setDifficulty]=useState(null);
  const quizIdRef=useRef(loc.state?.quizId||null);
  const [selectedLetters,setSelectedLetters]=useState({});
  const [selectedTexts,setSelectedTexts]=useState({});

  const [timerEnabled]=useState(!!(loc.state?.timerEnabled??(loc.state?.mode==='exam')));
  const [totalSeconds,setTotalSeconds]=useState(0);
  const [remainingSeconds,setRemainingSeconds]=useState(0);
  const [paused,setPaused]=useState(false);
  const [timeWarningDismissed,setTimeWarningDismissed]=useState(false);
  const autoSubmittedRef=useRef(false);
  const quizStartedAtRef=useRef(null);

  useEffect(()=>{
    const qid=quizIdRef.current;
    const url=qid?'/quiz/'+qid:'/quiz/latest';
    api.get(url).then(r=>{
      const d=r.data;
      if(Array.isArray(d)){
        setQuiz(d);
      }else if(d&&Array.isArray(d.questions)){
        setQuiz(d.questions);
        quizIdRef.current=d.quizId??qid??null;
        setDifficulty(d.difficulty??null);
      }else{
        setError('Invalid quiz format.');
      }
      setLoading(false);
    }).catch(()=>{setError('Unable to load quiz.');setLoading(false)});
  },[]);

  useEffect(()=>{
    if(loading||error||!quiz.length) return;
    quizStartedAtRef.current=Date.now();
    if(!timerEnabled) return;
    const seconds=quiz.length*SECONDS_PER_QUESTION;
    setTotalSeconds(seconds);
    setRemainingSeconds(seconds);
  },[loading,error,quiz.length,timerEnabled]);

  useEffect(()=>{
    if(!timerEnabled||loading||error||!quiz.length) return;
    const t=setInterval(()=>{
      if(paused) return;
      setRemainingSeconds(s=>{
        if(s<=1){
          clearInterval(t);
          if(!autoSubmittedRef.current){autoSubmittedRef.current=true;submitQuiz(true)}
          return 0;
        }
        return s-1;
      });
    },1000);
    return ()=>clearInterval(t);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  },[timerEnabled,loading,error,quiz.length,paused]);

  const lowTimeThreshold=Math.min(120,Math.floor(totalSeconds*0.2));
  const isLowTime=timerEnabled&&remainingSeconds>0&&remainingSeconds<=lowTimeThreshold;
  const showTimeWarning=isLowTime&&!timeWarningDismissed;
  const formattedTime=(()=>{const total=Math.max(0,remainingSeconds);const m=Math.floor(total/60),s=total%60;return `${m}:${String(s).padStart(2,'0')}`})();

  const totalQuestions=quiz.length;
  const answeredQuestions=useMemo(()=>{
    let count=0;
    quiz.forEach((q,i)=>{
      if(isMcq(q)){if(selectedLetters[i]) count++}
      else{if(selectedTexts[i]&&selectedTexts[i].trim()!=='') count++}
    });
    return count;
  },[quiz,selectedLetters,selectedTexts]);
  const progress=totalQuestions===0?0:Math.round((answeredQuestions/totalQuestions)*100);

  const elapsedSeconds=()=>{
    if(timerEnabled) return totalSeconds-remainingSeconds;
    if(quizStartedAtRef.current!==null) return Math.round((Date.now()-quizStartedAtRef.current)/1000);
    return 0;
  };

  const submitQuiz=async(auto=false)=>{
    if(!auto){
      const unanswered=[];
      quiz.forEach((q,i)=>{
        if(isMcq(q)){if(!selectedLetters[i]) unanswered.push(i+1)}
        else if(isTrueFalse(q)){if(!selectedTexts[i]) unanswered.push(i+1)}
        else{if(!selectedTexts[i]||selectedTexts[i].trim()==='') unanswered.push(i+1)}
      });
      if(unanswered.length){
        alert('Please answer every question.\n\nMissing: Q'+unanswered.join(', Q'));
        return;
      }
    }
    const answers=quiz.map((q,i)=>isMcq(q)?
      {questionId:q.id,selectedAnswerLetter:selectedLetters[i]??null,selectedAnswerText:null}:
      {questionId:q.id,selectedAnswerLetter:null,selectedAnswerText:selectedTexts[i]??null});
    const user=currentUser();
    const payload={userEmail:user.email||'guest@learn2play.local',answers,timeTakenSeconds:elapsedSeconds()};
    if(timerEnabled){payload.timedMode=true;payload.remainingSeconds=remainingSeconds}
    try{
      const qid=quizIdRef.current;
      const {data:result}=await api.post('/quiz/'+qid+'/attempts',payload);
      nav('/results',{state:{result,quiz,selectedLetters,selectedTexts,autoSubmitted:auto,timerEnabled,timeTakenSeconds:payload.timeTakenSeconds,quizId:qid}});
    }catch(e){
      alert('Failed to submit quiz.');
    }
  };

  return <div className="quiz-page">
    <div className="top-bar">
      <button className="back-btn" onClick={()=>nav('/upload')}>
        <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><path d="M19 12H5M12 19l-7-7 7-7"/></svg>
        Back to Upload
      </button>
      <button className="dashboard-btn" onClick={()=>nav('/dashboard')}>
        <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><rect x="3" y="3" width="7" height="7" rx="1.5"/><rect x="14" y="3" width="7" height="7" rx="1.5"/><rect x="14" y="14" width="7" height="7" rx="1.5"/><rect x="3" y="14" width="7" height="7" rx="1.5"/></svg>
        Dashboard
      </button>
    </div>

    <div className="hero">
      <h1>AI Generated Quiz</h1>
      <p>Complete all questions before submitting your quiz.</p>
      {difficultyLabel(difficulty)&&<span className="difficulty-badge">{difficultyIcon(difficulty)} {difficultyLabel(difficulty)} Difficulty</span>}
    </div>

    {!loading&&!error&&<div className="progress-card mat-mdc-card">
      <div className="progress-header"><span>Progress</span><span>{answeredQuestions} / {totalQuestions}</span></div>
      <div className="progress-bar"><div className="progress-fill" style={{width:progress+'%'}}/></div>
    </div>}

    {!loading&&!error&&timerEnabled&&<div className={'timer-card mat-mdc-card '+(isLowTime?'timer-low':'')}>
      <div className="timer-main">
        <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><circle cx="12" cy="12" r="9"/><path d="M12 7v5l3 3"/></svg>
        <div className="timer-info">
          <span className="timer-label">{paused?'Paused':'Time Remaining'}</span>
          <span className="timer-value">{formattedTime}</span>
        </div>
        <button className="pause-btn" onClick={()=>timerEnabled&&setPaused(p=>!p)}>
          {paused?<svg width="16" height="16" viewBox="0 0 24 24" fill="currentColor"><path d="M8 5v14l11-7z"/></svg>:<svg width="16" height="16" viewBox="0 0 24 24" fill="currentColor"><rect x="6" y="5" width="4" height="14"/><rect x="14" y="5" width="4" height="14"/></svg>}
          {paused?'Resume':'Pause'}
        </button>
      </div>
      {showTimeWarning&&<div className="timer-warning">
        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><path d="M12 9v4M12 17h.01M10.29 3.86 1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0Z"/></svg>
        <span>Only {formattedTime} left — wrap up soon!</span>
        <button className="dismiss-btn" onClick={()=>setTimeWarningDismissed(true)}>✖</button>
      </div>}
    </div>}

    {loading&&<div className="quiz-card mat-mdc-card state-card"><div className="state state-loading">
      <svg width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><path d="M6 2h12M6 22h12M6 2c0 6 4 6 4 10s-4 4-4 10M18 2c0 6-4 6-4 10s4 4 4 10"/></svg>
      <h2>Preparing Your Quiz...</h2>
      <p>AI is generating questions from your uploaded document.</p>
    </div></div>}

    {error&&!loading&&<div className="quiz-card mat-mdc-card state-card error"><div className="state state-error">
      <svg width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><circle cx="12" cy="12" r="10"/><path d="M12 8v4M12 16h.01"/></svg>
      <div><h2>Unable to Load Quiz</h2><p>{error}</p></div>
    </div></div>}

    {!loading&&!error&&quiz.length>0&&<div className="quiz-card mat-mdc-card">
      <div className="quiz-header"><h2>{totalQuestions} Questions</h2><p>Answer every question carefully.</p></div>

      <div className="question-list">
        {quiz.map((q,i)=>{
          const opts=q.options||{};
          return <div className="question-item" key={q.id||i}>
            <div className="q-meta"><span className="badge">{q.type}</span><span className="q-number">Question {i+1}</span></div>
            <p className="q-text">{q.question}</p>

            {isMcq(q)&&<div className="quiz-options">
              {['A','B','C','D'].map(l=><label className={'option-card '+(selectedLetters[i]===l?'selected':'')} key={l}>
                <input type="radio" name={'q'+i} checked={selectedLetters[i]===l} onChange={()=>setSelectedLetters({...selectedLetters,[i]:l})}/>
                <span className="option-letter">{l}</span>{opts[l]}
              </label>)}
            </div>}

            {isTrueFalse(q)&&<div className="quiz-options">
              {['True','False'].map(v=><label className={'option-card '+(selectedTexts[i]===v?'selected':'')} key={v}>
                <input type="radio" name={'q'+i} checked={selectedTexts[i]===v} onChange={()=>setSelectedTexts({...selectedTexts,[i]:v})}/>
                {v}
              </label>)}
            </div>}

            {isShortAnswer(q)&&<div className="field"><input className="text-answer" value={selectedTexts[i]||''} onChange={e=>setSelectedTexts({...selectedTexts,[i]:e.target.value})} placeholder="Type your answer here"/></div>}

            {isFillBlank(q)&&<div className="field"><input className="text-answer" value={selectedTexts[i]||''} onChange={e=>setSelectedTexts({...selectedTexts,[i]:e.target.value})} placeholder="Enter the missing word"/></div>}
          </div>;
        })}
      </div>

      <div className="quiz-summary">
        <div className="summary-card"><h3>Questions</h3><h1>{totalQuestions}</h1></div>
        <div className="summary-card"><h3>Answered</h3><h1>{answeredQuestions}</h1></div>
        <div className="summary-card"><h3>Progress</h3><h1>{progress}%</h1></div>
      </div>

      <div className="submit-section">
        <button className="submit-btn" onClick={()=>submitQuiz(false)}>
          <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><path d="M9 11l3 3L22 4"/><path d="M21 12v7a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11"/></svg>
          Submit Quiz
        </button>
        <button className="dashboard-btn" onClick={()=>nav('/dashboard')}>
          <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><rect x="3" y="3" width="7" height="7" rx="1.5"/><rect x="14" y="3" width="7" height="7" rx="1.5"/><rect x="14" y="14" width="7" height="7" rx="1.5"/><rect x="3" y="14" width="7" height="7" rx="1.5"/></svg>
          Dashboard
        </button>
        <button className="upload-btn" onClick={()=>nav('/upload')}>
          <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><path d="M12 16V4M12 4L7 9M12 4l5 5"/><path d="M4 16v3a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2v-3"/></svg>
          Upload Another PDF
        </button>
      </div>
    </div>}

    {!loading&&!error&&quiz.length===0&&<div className="quiz-card mat-mdc-card state-card"><div className="state">
      <svg width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><circle cx="11" cy="11" r="8"/><path d="M21 21l-4.35-4.35"/></svg>
      <h2>No Questions Generated</h2>
      <p>The AI couldn't generate any questions from the uploaded document.</p>
      <button className="btn primary" onClick={()=>nav('/upload')}>Upload Another File</button>
    </div></div>}
  </div>;
}
