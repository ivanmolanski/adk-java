'use client'

import Link from 'next/link'
import { usePathname } from 'next/navigation'
import { Button } from './ui/button'
import { ModeToggle } from '@/components/mode-toggle'

const routes = [
  {
    href: '/dashboard',
    label: 'Dashboard',
  },
  {
    href: '/research',
    label: 'Research Center', 
  },
  {
    href: '/command-center',
    label: 'Command Center',
  },
  {
    href: '/content-studio',
    label: 'Content Studio',
  }
]

export function Navbar() {
  const pathname = usePathname()

  return (
    <header className="border-b">
      <div className="container flex h-16 items-center justify-between px-4">
        <Link href="/" className="font-bold">
          MDAesthetics Viral Forge
        </Link>
        <nav className="flex items-center space-x-4">
          {routes.map((route) => (
            <Button
              key={route.href}
              variant={pathname === route.href ? 'default' : 'ghost'}
              asChild
            >
              <Link href={route.href}>
                {route.label}
              </Link>
            </Button>
          ))}
          <ModeToggle />
        </nav>
      </div>
    </header>
  )
}