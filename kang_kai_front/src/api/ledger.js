import request from './request'
import axios from 'axios'

const baseURL = '/api'

export const queryLedger = (params) => request.get('/ledger', { params })

export const exportLedger = (params) => {
  const token = localStorage.getItem('token')
  return axios.get(`${baseURL}/ledger/export`, {
    params,
    responseType: 'blob',
    headers: { Authorization: `Bearer ${token}` }
  })
}

export const saveLedgerSign = (data) => request.post('/ledger/sign', data)
