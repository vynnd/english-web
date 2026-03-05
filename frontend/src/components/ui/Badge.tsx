import { type ReactNode } from 'react'

type Color = 'blue' | 'green' | 'yellow' | 'red' | 'purple' | 'gray'

interface Props {
  children: ReactNode
  color?: Color
  className?: string
}

const colorClasses: Record<Color, string> = {
  blue: 'bg-blue-100 text-blue-700',
  green: 'bg-green-100 text-green-700',
  yellow: 'bg-yellow-100 text-yellow-700',
  red: 'bg-red-100 text-red-700',
  purple: 'bg-purple-100 text-purple-700',
  gray: 'bg-gray-100 text-gray-600',
}

export function Badge({ children, color = 'gray', className = '' }: Props) {
  return (
    <span className={`inline-flex items-center rounded-full px-2.5 py-0.5 text-xs font-medium ${colorClasses[color]} ${className}`}>
      {children}
    </span>
  )
}
