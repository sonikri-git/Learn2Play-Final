import axios from 'axios';
export const api = axios.create({baseURL: import.meta.env.VITE_API_URL || 'http://localhost:8080'});
api.interceptors.request.use(c=>{const t=localStorage.getItem('accessToken'); if(t) c.headers.Authorization=`Bearer ${t}`; return c;});
export const currentUser=()=>{try{return JSON.parse(localStorage.getItem('user')||'{}')}catch{return {}}};
export const logout=()=>{localStorage.removeItem('user');localStorage.removeItem('accessToken');};
