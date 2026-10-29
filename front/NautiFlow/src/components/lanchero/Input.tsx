import { useId } from "react";
import type { InputHTMLAttributes } from "react";

type Props = { label: string; } & InputHTMLAttributes<HTMLInputElement>;

export default function Input({ label, className = "", ...rest }: Props) {
    const id = useId();

    return (
        <div className="flex flex-col gap-1">
            <label htmlFor={id} className="text-sm font-semibold leading-[22px] text-text-primary">
                {label}
            </label>
            <input id={id}
                className={`w-full rounded-m border border-border-secondary bg-bg-surface-primary px-3 py-2 text-sm leading-[22px] text-text-primary placeholder:text-text-disable ${className}`}
                {...rest}
            />
        </div>
    );
}