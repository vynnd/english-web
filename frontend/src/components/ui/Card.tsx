import { type ReactNode } from 'react'

interface Props {
  children: ReactNode
  className?: string
}

export function Card({ children, className = '' }: Props) {
  return (
    <div className={`rounded-xl border border-gray-200 bg-white shadow-sm ${className}`}>
      {children}
    </div>
  )
}

export function CardHeader({ children, className = '' }: Props) {
  return <div className={`border-b border-gray-100 px-5 py-4 ${className}`}>{children}</div>
}

export function CardBody({ children, className = '' }: Props) {
  return <div className={`px-5 py-4 ${className}`}>{children}</div>
}
