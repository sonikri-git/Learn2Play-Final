import React,{useRef,useState} from 'react';
import {useNavigate} from 'react-router-dom';
import Navbar from '../components/Navbar';
import {api,currentUser} from '../api';
import '../styles/upload.scss';

const allowed=['pdf','docx','pptx','txt','md','rtf','csv','html','htm'];
const questionCountPresets=[5,10,15,20,25];
const minQuestionCount=1,maxQuestionCount=50;

export default function UploadPage(){
  const nav=useNavigate();
  const [files,setFiles]=useState([]);
  const [drag,setDrag]=useState(false);
  const [types,setTypes]=useState([]);
  const [count,setCount]=useState(10);
  const [mode,setMode]=useState('practice');
  const [difficulty,setDifficulty]=useState('intermediate');
  const [progress,setProgress]=useState(0);
  const [uploading,setUploading]=useState(false);
  const [isGenerating,setIsGenerating]=useState(false);
  const [readyToStart,setReadyToStart]=useState(false);
  const [relevance,setRelevance]=useState(null);
  const [lastQuizId,setLastQuizId]=useState(null);

  const validateFile=f=>{
    const ext=f.name.split('.').pop()?.toLowerCase()||'';
    if(!allowed.includes(ext)) return 'Unsupported file type. Allowed: PDF, DOCX, PPTX, TXT, MD, RTF, CSV, HTML';
    if(f.size>100*1024*1024) return 'File exceeds 100MB';
    return null;
  };

  const add=fs=>{
    const valid=[];
    [...fs].forEach(f=>{
      const err=validateFile(f);
      if(err) alert(f.name+': '+err); else valid.push(f);
    });
    setFiles(valid);
  };

  const toggle=t=>setTypes(x=>x.includes(t)?x.filter(v=>v!==t):[...x,t]);

  const clear=()=>{
    setFiles([]);setProgress(0);setTypes([]);setMode('practice');setDifficulty('intermediate');
    setCount(10);setIsGenerating(false);setReadyToStart(false);setRelevance(null);
  };

  const decrementCount=()=>setCount(c=>Math.max(minQuestionCount,c-1));
  const incrementCount=()=>setCount(c=>Math.min(maxQuestionCount,c+1));
  const onQuestionCountInput=e=>{
    const raw=e.target.value;const value=Number(raw);
    if(!raw||isNaN(value)){setCount(minQuestionCount);return}
    setCount(Math.min(maxQuestionCount,Math.max(minQuestionCount,Math.round(value))));
  };

  const startUpload=async()=>{
    if(!files.length){alert('No files selected');return}
    if(!types.length){alert('Please select at least one question type');return}
    setUploading(true);setIsGenerating(true);setProgress(0);
    let selectedType='short';
    if(types.includes('MCQ')) selectedType='mcq';
    else if(types.includes('TRUE_FALSE')) selectedType='truefalse';
    else if(types.includes('FILL_BLANKS')) selectedType='fill';
    else if(types.includes('SHORT_ANSWER')) selectedType='short';
    try{
      let last;
      const total=files.length;
      for(let idx=0;idx<total;idx++){
        const file=files[idx];
        const fd=new FormData();
        fd.append('file',file);
        fd.append('questionType',selectedType);
        fd.append('difficulty',difficulty);
        fd.append('questionCount',String(count));
        fd.append('email',currentUser().email||'');
        const r=await api.post('/upload',fd,{onUploadProgress:e=>{
          const value=Math.round(e.loaded/(e.total||1)*100);
          setProgress(Math.round(((idx+value/100)/total)*100));
        }});
        last=r.data;
      }
      if(last&&typeof last.relevanceAccuracy==='number') setRelevance(last.relevanceAccuracy);
      setLastQuizId(last?.quizId||null);
      setIsGenerating(false);
      setReadyToStart(true);
    }catch(e){
      alert(typeof e.response?.data==='string'?e.response.data:'Upload failed');
      setIsGenerating(false);
    }finally{
      setUploading(false);
    }
  };

  const relevanceClass=relevance==null?'':relevance>=80?'high':relevance>=60?'medium':'low';

  return <><Navbar/><div className="upload-page">
    <div className="upload-card">
      <h2>Upload Study Material</h2>
      <p className="subtitle">Drag &amp; drop or select your files to generate AI-powered quizzes</p>

      <label className={'upload-zone '+(drag?'active':'')}
        onDragOver={e=>{e.preventDefault();setDrag(true)}}
        onDragLeave={e=>{e.preventDefault();setDrag(false)}}
        onDrop={e=>{e.preventDefault();setDrag(false);add(e.dataTransfer.files)}}>
        <input type="file" multiple hidden accept=".pdf,.docx,.pptx,.txt,.md,.rtf,.csv,.html,.htm" onChange={e=>add(e.target.files)}/>
        <div className="upload-content">
          <h3>📂 Drop files here</h3>
          <p>or click to browse</p>
          <small>Supported: PDF, DOCX, PPTX, TXT, MD, RTF, CSV, HTML</small>
        </div>
      </label>

      {files.length?<div className="file-list">{files.map((f,i)=>
        <div className="file-item" key={f.name+i}>
          <div className="file-info">
            <span className="file-icon">📄</span>
            <div><strong>{f.name}</strong><p>{Math.round(f.size/1024)} KB</p></div>
          </div>
          <button className="remove-btn" onClick={()=>setFiles(files.filter((_,x)=>x!==i))}>✖</button>
        </div>)}
      </div>:<p className="empty-text">No files selected</p>}

      <div className="question-section">
        <h3>Select Question Types</h3>
        <div className="question-options">
          {[['MCQ','MCQ'],['TRUE_FALSE','True / False'],['SHORT_ANSWER','Short Answer'],['FILL_BLANKS','Fill in the Blanks']].map(([v,l])=>
            <button className={'type-btn '+(types.includes(v)?'active':'')} onClick={()=>toggle(v)} key={v}>{l}</button>)}
        </div>
      </div>

      <div className="question-section">
        <h3>Number of Questions</h3>
        <div className="question-options">
          {questionCountPresets.map(n=><button className={'type-btn '+(count===n?'active':'')} onClick={()=>setCount(n)} key={n}>{n}</button>)}
        </div>
        <div className="count-stepper">
          <button type="button" className="step-btn" onClick={decrementCount} disabled={count<=minQuestionCount}>−</button>
          <input type="number" className="count-input" min={minQuestionCount} max={maxQuestionCount} value={count} onChange={onQuestionCountInput}/>
          <button type="button" className="step-btn" onClick={incrementCount} disabled={count>=maxQuestionCount}>+</button>
          <span className="count-label">questions</span>
        </div>
        <p className="mode-hint">Pick a preset or type an exact number ({minQuestionCount}–{maxQuestionCount}).</p>
      </div>

      <div className="question-section">
        <h3>Quiz Mode</h3>
        <div className="question-options">
          <button className={'type-btn '+(mode==='practice'?'active':'')} onClick={()=>setMode('practice')}>🧘 Practice Mode</button>
          <button className={'type-btn '+(mode==='exam'?'active':'')} onClick={()=>setMode('exam')}>⏱️ Exam Mode (Timed)</button>
        </div>
        <p className="mode-hint">{mode==='practice'?'Self-paced — take as long as you need.':"Timed — you'll get roughly 1.2 minutes per question, and the quiz auto-submits when time runs out."}</p>
      </div>

      <div className="question-section">
        <h3>Difficulty Level</h3>
        <div className="question-options">
          {[['easy','🟢 Easy'],['intermediate','🟡 Intermediate'],['hard','🔴 Hard']].map(([v,l])=>
            <button className={'type-btn '+(difficulty===v?'active':'')} onClick={()=>setDifficulty(v)} key={v}>{l}</button>)}
        </div>
        <p className="mode-hint">
          {difficulty==='easy'&&'Straightforward questions based on clearly stated facts.'}
          {difficulty==='intermediate'&&'A balanced mix — requires connecting a couple of details.'}
          {difficulty==='hard'&&'Challenging questions with subtle, tricky details.'}
        </p>
      </div>

      {!readyToStart&&!isGenerating&&<div className="action-buttons">
        <button type="button" className="dashboard-btn" onClick={()=>nav('/dashboard')}>DashBoard</button>
        <button className="upload-btn" onClick={startUpload}>Upload &amp; Generate Quiz</button>
        <button className="clear-btn" onClick={clear}>Clear</button>
      </div>}

      {isGenerating&&<div className="generation-message">
        <span className="material-icons" aria-hidden="true">⏳</span>
        <h3>Generating Questions...</h3>
        <p>Your document was uploaded successfully. Please wait while AI generates your quiz.</p>
        <p>This may take up to a minute for larger documents.</p>
      </div>}

      {readyToStart&&<div className="quiz-ready-panel">
        <div className="ready-icon">✓</div>
        <h3>Quiz ready</h3>
        <p>{count} question{count===1?'':'s'} generated.</p>
        {relevance!==null&&<div className={'relevance-badge '+relevanceClass}>
          <span className="relevance-value">{relevance}%</span>
          <span className="relevance-label">AI relevance</span>
        </div>}
        {relevance!==null&&<p className="relevance-hint">Share of AI-proposed questions that passed quality review for this quiz.</p>}
        <button className="upload-btn" onClick={()=>nav('/quiz',{state:{quizId:lastQuizId,mode}})}>Start Quiz</button>
      </div>}

      {uploading&&<div className="progress-wrapper">
        <div className="progress-bar"><div className="progress-fill" style={{width:progress+'%'}}/></div>
        <p>Uploading... {progress}%</p>
      </div>}
    </div>

    <p className="footer">© 2026 Learn2Play • Capstone Project</p>
  </div></>;
}
